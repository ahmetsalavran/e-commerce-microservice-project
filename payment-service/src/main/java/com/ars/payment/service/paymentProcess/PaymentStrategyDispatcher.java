package com.ars.payment.service.paymentProcess;

import com.ars.contract.strategy.PaymentStrategy;
import com.ars.payment.service.paymentProcess.model.PaymentCommand;
import com.ars.payment.service.paymentProcess.model.PaymentProcessResult;
import org.springframework.stereotype.Component;

@Component
public class PaymentStrategyDispatcher {

    private final PaymentStrategyRegistry registry;

    public PaymentStrategyDispatcher(PaymentStrategyRegistry registry) {
        this.registry = registry;
    }

    public PaymentProcessResult dispatch(PaymentStrategy key, PaymentCommand command) {
        return registry.getRequired(key).process(command);
    }
}

