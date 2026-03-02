package com.ars.order.messaging.listeners;

import com.ars.contract.messaging.Topics;
import com.ars.contract.messaging.events.InventoryRejectedEvent;
import com.ars.order.service.OrderCompensateService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InventoryRejectedListener {

    private final OrderCompensateService orderCompanseService;

    @KafkaListener(
            topics = "${app.topics.inventory-rejected:" + Topics.INVENTORY_REJECTED + "}",
            containerFactory = "inventoryRejectedListenerFactory"
    )
    public void onMessage(InventoryRejectedEvent event, Acknowledgment ack) {
        orderCompanseService.onInventoryRejected(event, ack::acknowledge);
    }


}
