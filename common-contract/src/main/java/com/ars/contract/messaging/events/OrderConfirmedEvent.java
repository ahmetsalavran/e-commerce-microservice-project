package com.ars.contract.messaging.events;

import com.ars.contract.strategy.PaymentStrategy;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderConfirmedEvent(
        String eventId,
        long orderId,
        long customerId,
        @JsonFormat(shape = JsonFormat.Shape.STRING) Instant occurredAt,
        String orderType,
        PaymentStrategy paymentStrategy,
        List<OrderItemDto> items,
        BigDecimal totalPrice
) {}
