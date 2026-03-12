package com.ars.payment.service.paymentProcess;

import com.ars.contract.strategy.PaymentStrategy;
import com.ars.payment.service.paymentProcess.model.PaymentCommand;
import com.ars.payment.service.paymentProcess.model.PaymentProcessResult;

public interface PaymentProcessingStrategy {
    PaymentStrategy key();

    PaymentProcessResult process(PaymentCommand command);
}

