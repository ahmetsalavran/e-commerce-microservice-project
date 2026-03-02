package com.ars.order.service.impl.strategy.confirm;

import com.ars.contract.messaging.events.OrderConfirmedPartitionedEvent;
import com.ars.contract.messaging.events.OrderItemDto;
import com.ars.core.infrastructure.outbox.messaging.OutboxCreatedEvent;
import com.ars.core.infrastructure.outbox.entity.OutboxEvent;
import com.ars.core.infrastructure.outbox.service.OutboxEventService;
import com.ars.core.infrastructure.web.error.InternalServerException;
import com.ars.order.models.entities.OrdersCart;
import com.ars.order.service.OrderConfirmPublishStrategy;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class PartitionedOrderConfirmPublishStrategy implements OrderConfirmPublishStrategy {
    private static final String AGGREGATE_TYPE_ORDER = "ORDER";
    private static final String EVENT_TYPE_ORDER_CONFIRMED_PARTITIONED = "ORDER_CONFIRMED_PARTITIONED";
    private static final int PARTITIONED_SEGMENT_COUNT = 3;

    private final OutboxEventService outboxEventService;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

    public PartitionedOrderConfirmPublishStrategy(OutboxEventService outboxEventService,
                                                  ObjectMapper objectMapper,
                                                  ApplicationEventPublisher eventPublisher) {
        this.outboxEventService = outboxEventService;
        this.objectMapper = objectMapper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void publish(OrdersCart order, List<OrderItemDto> items, BigDecimal total) {
        try {
            String batchId = UUID.randomUUID().toString();
            List<List<OrderItemDto>> segments = splitByProductRange(items, PARTITIONED_SEGMENT_COUNT);

            for (int segmentNo = 0; segmentNo < segments.size(); segmentNo++) {
                List<OrderItemDto> segmentItems = segments.get(segmentNo);
                if (segmentItems.isEmpty()) {
                    continue;
                }

                OrderConfirmedPartitionedEvent event = new OrderConfirmedPartitionedEvent(
                        batchId + "-s" + segmentNo,
                        batchId,
                        segmentNo,
                        PARTITIONED_SEGMENT_COUNT,
                        order.getOrderId(),
                        order.getCustomerId(),
                        Instant.now(),
                        order.getOrderType().toString(),
                        segmentItems,
                        total
                );

                OutboxEvent saved = outboxEventService.createAndSave(
                        AGGREGATE_TYPE_ORDER,
                        String.valueOf(order.getOrderId()),
                        EVENT_TYPE_ORDER_CONFIRMED_PARTITIONED,
                        order.getOrderId() + ":" + segmentNo,
                        String.valueOf(order.getOrderType()),
                        objectMapper.writeValueAsString(event)
                );

                eventPublisher.publishEvent(new OutboxCreatedEvent(
                        saved.getId(),
                        saved.getEventType(),
                        saved.getKey(),
                        saved.getPayload()
                ));
            }
        } catch (JsonProcessingException e) {
            throw new InternalServerException("Failed to serialize OrderConfirmedPartitionedEvent", e);
        }
    }

    private List<List<OrderItemDto>> splitByProductRange(List<OrderItemDto> items, int segmentCount) {
        List<List<OrderItemDto>> buckets = new ArrayList<>(segmentCount);
        for (int i = 0; i < segmentCount; i++) {
            buckets.add(new ArrayList<>());
        }
        for (OrderItemDto item : items) {
            int segmentNo = segmentNoByProductIdRange(item.productId());
            buckets.get(segmentNo).add(item);
        }
        return buckets;
    }

    private int segmentNoByProductIdRange(long productId) {
        if (productId >= 1 && productId <= 50) {
            return 0;
        }
        if (productId >= 51 && productId <= 100) {
            return 1;
        }
        return 2;
    }
}
