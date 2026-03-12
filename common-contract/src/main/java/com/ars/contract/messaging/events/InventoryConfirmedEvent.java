package com.ars.contract.messaging.events;

import com.ars.contract.strategy.PaymentStrategy;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record InventoryConfirmedEvent(
        String eventId,
        long orderId,
        long customerId,
        String strategyKey,
        PaymentStrategy paymentStrategy,
        boolean success,
        String message,
        List<Item> items,
        OffsetDateTime decidedAt,
        BigDecimal totalPrice
) {
    public record Item(long productId, int requestedQty, int deductedQty) {}
}
