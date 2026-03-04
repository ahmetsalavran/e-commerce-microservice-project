package com.ars.order.service.impl.factory;

import com.ars.contract.strategy.InventoryStrategy;
import com.ars.order.service.OrderConfirmPublishStrategy;
import com.ars.order.service.impl.strategy.confirm.PartitionedOrderConfirmPublishStrategy;
import com.ars.order.service.impl.strategy.confirm.SingleOrderConfirmPublishStrategy;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OrderConfirmPublishStrategyFactory {

    private final Map<InventoryStrategy, OrderConfirmPublishStrategy> strategies;

    public OrderConfirmPublishStrategyFactory(
            SingleOrderConfirmPublishStrategy single,
            PartitionedOrderConfirmPublishStrategy partitioned
    ) {
        this.strategies = Map.of(
                InventoryStrategy.ALL_OR_NOTHING, single,
                InventoryStrategy.PARTIAL_OK_BUT_NOT_ZERO, single,
                InventoryStrategy.PARTITIONED_BEST_EFFORT, partitioned
        );
    }

    public OrderConfirmPublishStrategy getRequired(InventoryStrategy strategy) {
        OrderConfirmPublishStrategy publishStrategy = strategies.get(strategy);
        if (publishStrategy == null) {
            throw new IllegalArgumentException("Bu sipariş tipi için yayın stratejisi bulunamadı: " + strategy);
        }
        return publishStrategy;
    }
}
