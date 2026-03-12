package com.ars.payment.messaging.listeners;

import com.ars.contract.messaging.Topics;
import com.ars.contract.messaging.events.PaymentChargeRequestedEvent;
import com.ars.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentChargeRequestedListener {

    private final PaymentService paymentService;

    @KafkaListener(
            topics = "${app.topics.payment-charge-requested:" + Topics.PAYMENT_CHARGE_REQUESTED + "}",
            containerFactory = "paymentChargeRequestedListenerFactory"
    )
    public void onMessage(PaymentChargeRequestedEvent event, Acknowledgment ack) {
        paymentService.handle(event, ack::acknowledge);
    }
}
