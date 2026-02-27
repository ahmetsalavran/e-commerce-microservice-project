package com.ars.inventory.messaging.inner;

import com.ars.inventory.messaging.model.InventoryEventType;

public record OutboxCreatedEvent(
        Long outboxRowId,
        InventoryEventType eventType,
        String key,
        String payload
) {}
