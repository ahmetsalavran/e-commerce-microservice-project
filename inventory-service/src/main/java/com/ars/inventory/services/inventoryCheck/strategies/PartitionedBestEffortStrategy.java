package com.ars.inventory.services.inventoryCheck.strategies;

import com.ars.contract.strategy.InventoryStrategy;
import com.ars.inventory.models.entities.ProductStock;
import com.ars.inventory.repository.ProductStockRepository;
import com.ars.inventory.services.inventoryCheck.model.DeductResult;
import com.ars.inventory.services.inventoryCheck.model.StrategyCommand;
import jakarta.persistence.EntityManager;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PartitionedBestEffortStrategy implements com.ars.inventory.services.inventoryCheck.InventoryStrategy {

    private static final int MAX_RETRY = 3;
    private final ProductStockRepository stockRepo;
    private final EntityManager entityManager;

    @Override
    public InventoryStrategy key() {
        return InventoryStrategy.PARTITIONED_BEST_EFFORT;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public DeductResult deduct(StrategyCommand command) {
        List<DeductResult.ItemDeducted> deducted = new ArrayList<>();

        for (StrategyCommand.Item item : command.items()) {
            boolean deductedThisItem = false;

            for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
                try {
                    ProductStock stock = stockRepo.findById(item.productId()).orElse(null);
                    if (stock == null) {
                        return reject(command, "Stok kaydı bulunamadı. productId=" + item.productId(), deducted);
                    }
                    if (stock.getAvailable() < item.qty()) {
                        return reject(command, "Yetersiz stok. productId=" + item.productId() + " qty=" + item.qty(), deducted);
                    }

                    stock.setAvailable(stock.getAvailable() - item.qty());
                    stockRepo.saveAndFlush(stock);
                    deducted.add(new DeductResult.ItemDeducted(item.productId(), item.qty(), item.qty()));
                    deductedThisItem = true;
                    break;
                } catch (ObjectOptimisticLockingFailureException | OptimisticLockException ex) {
                    entityManager.clear();
                }
            }

            if (!deductedThisItem) {
                return reject(command, "Ürün ayrılamadı (optimistic lock, max 3 retry). productId=" + item.productId(), deducted);
            }
        }

        return new DeductResult(
                command.eventId(),
                command.orderId(),
                InventoryStrategy.PARTITIONED_BEST_EFFORT.name(),
                true,
                "Itemler versiyon kontrollü düşüldü",
                deducted,
                OffsetDateTime.now()
        );
    }

    private DeductResult reject(StrategyCommand command, String message, List<DeductResult.ItemDeducted> deducted) {
        return new DeductResult(
                command.eventId(),
                command.orderId(),
                InventoryStrategy.PARTITIONED_BEST_EFFORT.name(),
                false,
                message,
                deducted,
                OffsetDateTime.now()
        );
    }
}
