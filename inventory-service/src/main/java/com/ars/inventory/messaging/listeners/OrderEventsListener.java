package com.ars.inventory.messaging.listeners;

import com.ars.inventory.models.OrderConfirmedEvent;
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

    @KafkaListener(topics = "order.confirmed", containerFactory = "orderListenerFactory")
    public void onMessage(OrderConfirmedEvent event, Acknowledgment ack) {
        inventoryService.handle(event, ack::acknowledge);
    }


}
