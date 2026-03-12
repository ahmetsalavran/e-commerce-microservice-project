package com.ars.payment.service;

import com.ars.contract.messaging.events.PaymentChargeRequestedEvent;

import java.math.BigDecimal;

public interface PaymentService {
    void handle(PaymentChargeRequestedEvent event, Runnable ackAfterCommit);

    BigDecimal topUp(long customerId, BigDecimal amount);
}
