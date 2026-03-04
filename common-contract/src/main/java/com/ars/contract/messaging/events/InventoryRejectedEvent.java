package com.ars.contract.messaging.events;

import java.time.OffsetDateTime;
import java.util.List;

public record InventoryRejectedEvent(
        String eventId,
        long orderId,
        String strategyKey,
        boolean success,
        String message,
        List<Item> items,
        OffsetDateTime decidedAt
) {
    public record Item(long productId, int requestedQty, int deductedQty) {}
}
