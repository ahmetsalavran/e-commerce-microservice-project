package com.ars.inventory.messaging.listeners;

import com.ars.contract.messaging.Topics;
import com.ars.contract.messaging.events.OrderConfirmedEvent;
import com.ars.contract.messaging.events.OrderConfirmedPartitionedEvent;
import com.ars.inventory.services.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class OrderEventsListener {
    private static final Logger logger = LoggerFactory.getLogger(OrderEventsListener.class);
    private final InventoryService inventoryService;

    public OrderEventsListener(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @KafkaListener(topics = "${app.topics.order-confirmed:" + Topics.ORDER_CONFIRMED + "}", containerFactory = "orderListenerFactory")
    public void onMessage(OrderConfirmedEvent event, Acknowledgment ack) {
        inventoryService.handle(event, ack::acknowledge);
    }

    @KafkaListener(
            topics = "${app.topics.order-confirmed-partitioned:" + Topics.ORDER_CONFIRMED_PARTITIONED + "}",
            containerFactory = "partitionedOrderListenerFactory"
    )
    public void onPartitionedMessage(OrderConfirmedPartitionedEvent event, Acknowledgment ack) {
        OrderConfirmedEvent mapped = new OrderConfirmedEvent(
                event.eventId(),
                event.orderId(),
                event.customerId(),
                event.createdAt(),
                event.orderType(),
                event.paymentStrategy(),
                event.items(),
                event.totalPrice()
        );
        inventoryService.handle(mapped, ack::acknowledge);
    }


}
