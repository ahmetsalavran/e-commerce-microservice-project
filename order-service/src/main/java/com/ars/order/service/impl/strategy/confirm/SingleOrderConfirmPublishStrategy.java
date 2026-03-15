package com.ars.order.service.impl.strategy.confirm;

import com.ars.contract.messaging.events.OrderConfirmedEvent;
import com.ars.contract.messaging.events.OrderItemDto;
import com.ms.core.infrastructure.outbox.messaging.OutboxCreatedEvent;
import com.ms.core.infrastructure.outbox.entity.OutboxEvent;
import com.ms.core.infrastructure.outbox.service.OutboxEventService;
import com.ms.core.infrastructure.web.error.InternalServerException;
import com.ars.order.models.entities.OrdersCart;
import com.ars.order.service.OrderConfirmPublishStrategy;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class SingleOrderConfirmPublishStrategy implements OrderConfirmPublishStrategy {
    private static final String AGGREGATE_TYPE_ORDER = "ORDER";
    private static final String EVENT_TYPE_ORDER_CONFIRMED = "ORDER_CONFIRMED";

    private final OutboxEventService outboxEventService;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

    public SingleOrderConfirmPublishStrategy(OutboxEventService outboxEventService,
                                             ObjectMapper objectMapper,
                                             ApplicationEventPublisher eventPublisher) {
        this.outboxEventService = outboxEventService;
        this.objectMapper = objectMapper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void publish(OrdersCart order, List<OrderItemDto> items) {
        try {
            OrderConfirmedEvent event = new OrderConfirmedEvent(
                    UUID.randomUUID().toString(),
                    order.getOrderId(),
                    order.getCustomerId(),
                    Instant.now(),
                    order.getOrderType().toString(),
                    order.getPaymentStrategy(),
                    items,
                    BigDecimal.ZERO
            );

            OutboxEvent saved = outboxEventService.createAndSave(
                    AGGREGATE_TYPE_ORDER,
                    String.valueOf(order.getOrderId()),
                    EVENT_TYPE_ORDER_CONFIRMED,
                    String.valueOf(order.getOrderId()),
                    String.valueOf(order.getOrderType()),
                    objectMapper.writeValueAsString(event)
            );

            eventPublisher.publishEvent(new OutboxCreatedEvent(
                    saved.getId(),
                    saved.getEventType(),
                    saved.getKey(),
                    saved.getPayload()
            ));
        } catch (JsonProcessingException e) {
            throw new InternalServerException("Sipariş onay event'i serileştirilemedi.", e);
        }
    }
}
