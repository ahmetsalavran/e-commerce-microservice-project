package com.ars.contract.messaging.events;

import com.ars.contract.strategy.PaymentStrategy;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PaymentChargeRequestedEvent(
        String eventId,
        long orderId,
        long customerId,
        PaymentStrategy paymentStrategy,
        BigDecimal amount,
        OffsetDateTime requestedAt
) {
}
