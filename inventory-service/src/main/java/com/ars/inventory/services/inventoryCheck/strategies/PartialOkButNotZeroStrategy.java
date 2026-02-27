package com.ars.inventory.services.inventoryCheck.strategies;

import com.ars.inventory.exceptions.BusinessRejectException;
import com.ars.inventory.repositories.ProductStockRepository;
import com.ars.inventory.services.inventoryCheck.InventoryStrategy;
import com.ars.inventory.services.inventoryCheck.model.DeductResult;
import com.ars.inventory.services.inventoryCheck.model.InventoryStrategyKey;
import com.ars.inventory.services.inventoryCheck.model.StrategyCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PartialOkButNotZeroStrategy implements InventoryStrategy {

    private final ProductStockRepository stockRepo;

    @Override
    public InventoryStrategyKey key() {
        return InventoryStrategyKey.PARTIAL_OK_BUT_NOT_ZERO;
    }

    /**
    /**
     * Policy:
     * - Her üründen en az 1 düşülebilmeli; aksi halde tüm order fail.
     * - Eğer herkes >=1 ise: her üründen mümkün olan kadar düş (<= requested).
     * - DB yarışında tryDeduct 0 dönerse qty'yi 1'e kadar küçültüp tekrar dene.
     *
     * Not: Bu method çağrıldığı transaction içinde çalışır.
     * Herhangi bir üründe 1 bile düşemezsek BusinessRejectException fırlatırız -> rollback -> hiçbir şey düşülmez.
     */
    @Override
    public DeductResult deduct(StrategyCommand command) {

        // 1) Stable lock order: deadlock riskini azaltır
        List<StrategyCommand.Item> items = command.items().stream()
                .sorted(Comparator.comparing(StrategyCommand.Item::productId))
                .toList();

        // 2) Pre-check: her üründen en az 1 var mı?
        //    SELECT -> yarış olabilir; ama asıl garanti deduct aşamasında, 1 bile düşemezsek rollback.
        Map<Long, Integer> availableMap = loadAvailabilities(items);

        for (var item : items) {
            int available = availableMap.getOrDefault(item.productId(), 0);
            if (available <= 0) {
                throw new BusinessRejectException("NOT_ZERO_POLICY_FAIL productId=" + item.productId());
            }
        }

        // 3) Deduct: mümkün olan kadar düş (<= requested), ama en az 1 garantisini enforce et
        var deducted = new ArrayList<DeductResult.ItemDeducted>();

        for (var item : items) {
            int requested = item.qty();

            // İlk hedef: mümkün olan kadar (pre-check snapshot'a göre) ama asla 0 değil
            int snapshotAvail = availableMap.getOrDefault(item.productId(), 0);
            int target = Math.min(requested, snapshotAvail);
            if (target <= 0) target = 1;

            int actual = tryDeductDownToOne(item.productId(), target);

            // En az 1 garanti
            if (actual <= 0) {
                // Bu noktada 1 bile düşemedik -> tüm order fail
                throw new BusinessRejectException("NOT_ZERO_POLICY_FAIL productId=" + item.productId());
            }

            deducted.add(new DeductResult.ItemDeducted(item.productId(), requested, actual));
        }
        return new DeductResult(command.eventId(), command.orderId(),InventoryStrategyKey.PARTIAL_OK_BUT_NOT_ZERO.name(),true,"Itemler stock dan düşüldü", List.of(), OffsetDateTime.now());
    }

    /**
     * target'tan başlayarak 1'e kadar küçültüp tryDeduct dener.
     * İlk başarılı qty döner. Hiçbiri olmazsa 0 döner.
     */
    private int tryDeductDownToOne(long productId, int target) {
        int qty = Math.max(target, 1);

        while (qty >= 1) {
            int updated = stockRepo.tryDeduct(productId, qty);
            if (updated == 1) return qty;
            qty--;
        }
        return 0;
    }

    /**
     * Availabilities snapshot:
     * - Tek tek findById yapmamak için bulk fetch.
     * - Repo'da custom method yoksa findAllById ile de çalışır.
     */
    private Map<Long, Integer> loadAvailabilities(List<StrategyCommand.Item> items) {
        var ids = items.stream().map(StrategyCommand.Item::productId).distinct().toList();

        // ProductStockRepository extends JpaRepository<ProductStock, String> varsayımı:
        var stocks = stockRepo.findAllById(ids);

        Map<Long, Integer> map = new HashMap<>();
        for (var s : stocks) {
            map.put(s.getProductId(), s.getAvailable());
        }
        // olmayan productId'ler map'te yok -> 0 sayılacak
        return map;
    }


}
