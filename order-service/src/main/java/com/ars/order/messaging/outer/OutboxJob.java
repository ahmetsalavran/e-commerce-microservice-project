package com.ars.order.messaging.outer;

public record OutboxJob(
        Long outboxRowId,
        String eventType,
        String keyId,
        String payload
) {}
