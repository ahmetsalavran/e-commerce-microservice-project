package com.ars.order.messaging.listener;

import com.ars.order.models.eventModels.InventoryRejectedEvent;
import com.ars.order.service.OrderCompanseService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InventoryRejectedListener {

    private final OrderCompanseService orderCompanseService;

    @KafkaListener(
            topics = "inventory.rejected",
            containerFactory = "inventoryRejectedListenerFactory"
    )
    public void onMessage(InventoryRejectedEvent event, Acknowledgment ack) {
        orderCompanseService.onInventoryRejected(event, ack::acknowledge);
    }


}