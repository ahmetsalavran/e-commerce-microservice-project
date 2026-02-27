package com.ars.order.messaging.inner;

public record OutboxCreatedEvent(
        Long outboxRowId,
        String eventType,
        String key,
        String payload
) {}
