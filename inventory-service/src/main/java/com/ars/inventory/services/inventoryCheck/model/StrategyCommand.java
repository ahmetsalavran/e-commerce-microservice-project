package com.ars.inventory.services.inventoryCheck.model;

import java.util.List;

public record StrategyCommand(
        String eventId,
        long orderId,
        List<Item> items
) {
    public record Item(Long productId, int qty) {}
}

