package com.ars.inventory.services.inventoryCheck.model;

import java.time.OffsetDateTime;
import java.util.List;

public record DeductResult(
        String eventId,
        long orderId,
        String strategyKey,
        boolean success,
        String message,
        List<ItemDeducted> items,
        OffsetDateTime decidedAt
) {

    public static DeductResult success(String eventId, long orderId, String strategyKey, List<ItemDeducted> items) {
        return new DeductResult(eventId, orderId, strategyKey, true, "OK", items, OffsetDateTime.now());
    }

    public static DeductResult reject(String eventId, long orderId, String strategyKey, String message) {
        return new DeductResult(eventId, orderId, strategyKey, false, message, List.of(), OffsetDateTime.now());
    }

    public record ItemDeducted(long productId, int requestedQty, int deductedQty) {}
}