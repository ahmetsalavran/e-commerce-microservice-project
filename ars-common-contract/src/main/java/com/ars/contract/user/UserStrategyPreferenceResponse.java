package com.ars.contract.user;

import com.ars.contract.strategy.InventoryStrategy;
import com.ars.contract.strategy.PaymentStrategy;

public record UserStrategyPreferenceResponse(
        Long customerId,
        InventoryStrategy inventoryStrategy,
        PaymentStrategy paymentStrategy
) {}
