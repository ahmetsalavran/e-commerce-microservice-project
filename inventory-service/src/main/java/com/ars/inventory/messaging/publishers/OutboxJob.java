package com.ars.inventory.messaging.publishers;

import com.ars.inventory.messaging.model.InventoryEventType;

public record OutboxJob(
        Long outboxRowId,
        InventoryEventType eventType,
        String keyId,
        String payload
) {}
