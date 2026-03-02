package com.ars.order.service.impl.factory;

import com.ars.order.models.enums.CancelReason;
import com.ars.order.service.CancelOrderStrategy;
import com.ars.order.service.impl.strategy.cancel.InventoryFailedCancelStrategy;
import com.ars.order.service.impl.strategy.cancel.TimeoutCancelOrderStrategy;
import com.ars.order.service.impl.strategy.cancel.UserCancelOrderStrategy;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CancelOrderStrategyFactory {

    private final Map<CancelReason, CancelOrderStrategy> strategies;

    public CancelOrderStrategyFactory(
            UserCancelOrderStrategy user,
            InventoryFailedCancelStrategy inventory,
            TimeoutCancelOrderStrategy timeout
    ) {
        strategies = Map.of(
                CancelReason.USER_REQUEST, user,
                CancelReason.INVENTORY_FAILED, inventory,
                CancelReason.TIMEOUT, timeout
        );
    }

    public CancelOrderStrategy getStrategy(CancelReason reason) {
        return strategies.get(reason);
    }
}
