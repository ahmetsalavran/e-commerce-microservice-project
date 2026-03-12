package com.ars.payment.messaging.publishers;

import com.ars.contract.messaging.Topics;
import com.ars.contract.messaging.events.PaymentCompensationRequestedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentCompensationPublisher {

    private final KafkaTemplate<String, Object> paymentKafkaTemplate;

    @Value("${app.topics.inventory-compensate-requested:" + Topics.INVENTORY_COMPENSATE_REQUESTED + "}")
    private String inventoryCompensateRequestedTopic;

    public void publish(PaymentCompensationRequestedEvent event) {
        paymentKafkaTemplate.send(inventoryCompensateRequestedTopic, String.valueOf(event.orderId()), event);
    }
}

