package com.ars.inventory.models;

import java.time.Instant;
import java.util.List;

public record OrderConfirmedEvent(
        String eventId,
        long orderId,
        long customerId,
        Instant occurredAt,
        String orderType,
        List<Item> items
) {
    public record Item(long productId, int qty) {}
}
