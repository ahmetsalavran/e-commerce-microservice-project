package com.ars.contract.messaging.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderConfirmedPartitionedEvent(
        String eventId,
        String batchId,
        int segmentNo,
        int expectedSegments,
        long orderId,
        long customerId,
        Instant createdAt,
        String orderType,
        List<OrderItemDto> items,
        BigDecimal totalPrice
) {
}
