package com.ars.inventory.services.inventoryCheck.strategies;

import com.ars.inventory.models.entities.ProductStock;
import com.ars.inventory.repositories.ProductStockRepository;
import com.ars.inventory.services.inventoryCheck.InventoryStrategy;
import com.ars.inventory.services.inventoryCheck.model.DeductResult;
import com.ars.inventory.services.inventoryCheck.model.InventoryStrategyKey;
import com.ars.inventory.services.inventoryCheck.model.StrategyCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AllOrNothingStrategy implements InventoryStrategy {

    private final ProductStockRepository stockRepo;

    @Override
    public InventoryStrategyKey key() {
        return InventoryStrategyKey.ALL_OR_NOTHING;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public DeductResult deduct(StrategyCommand command) {

        // 0) Aynı productId birden fazla kez geldiyse qty topla (çok kritik)
        List<StrategyCommand.Item> items = command.items().stream()
                .sorted(Comparator.comparingLong(StrategyCommand.Item::productId))
                .toList();

        List<Long> ids = items.stream().map(StrategyCommand.Item::productId).toList();

        // 1) Pessimistic lock: satır kilitliyse BEKLER (senin istediğin davranış)
        List<ProductStock> locked = stockRepo.lockByProductIdIn(ids);

        /**
         * BEGIN;
         * select * from product_stock where product_id=2 for update; bunu db de çalıştırdığımda commit bekleniyor*/
        // 2) Eksik satır var mı? (Inventory DB'de row yok)
        if (locked.size() != ids.size()) {
            return new DeductResult(command.eventId(), command.orderId(),InventoryStrategyKey.ALL_OR_NOTHING.name(),false,"OUT_OF_STOCK missing_stock_row", List.of(), OffsetDateTime.now());
        }

        Map<Long, Integer> avail = locked.stream()
                .collect(Collectors.toMap(ProductStock::getProductId, ProductStock::getAvailable));

        // 3) Pre-check: herhangi biri yetmiyorsa UPDATE YAPMADAN reject dön (commit + ack safe)
        for (var item : items) {
            int a = avail.getOrDefault(item.productId(), 0);
            if (a < item.qty()) {
                return new DeductResult(command.eventId(), command.orderId(),InventoryStrategyKey.ALL_OR_NOTHING.name(),false,"OUT_OF_STOCK productId=" + item.productId() + " qty=" + item.qty(), List.of(), OffsetDateTime.now());

            }
        }

        // 4) Hepsi yeterli -> düş
        var deducted = new ArrayList<DeductResult.ItemDeducted>();
        for (var item : items) {
            int updated = stockRepo.tryDeduct(item.productId(), item.qty());
            if (updated == 0) {
                // lock altında beklemiyoruz ama safety: rare race / missing row
                return new DeductResult(command.eventId(), command.orderId(),InventoryStrategyKey.ALL_OR_NOTHING.name(),false,"OUT_OF_STOCK productId=" + item.productId() + " qty=" + item.qty(), List.of(), OffsetDateTime.now());
            }
            deducted.add(new DeductResult.ItemDeducted(item.productId(), item.qty(), item.qty()));
        }

        return new DeductResult(command.eventId(), command.orderId(),InventoryStrategyKey.ALL_OR_NOTHING.name(),true,"Itemler stock dan düşüldü", deducted, OffsetDateTime.now());

    }
}