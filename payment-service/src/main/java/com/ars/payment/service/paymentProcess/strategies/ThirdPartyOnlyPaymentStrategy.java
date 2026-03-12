package com.ars.payment.service.paymentProcess.strategies;

import com.ars.contract.strategy.PaymentStrategy;
import com.ars.payment.service.paymentProcess.PaymentProcessingStrategy;
import com.ars.payment.service.paymentProcess.model.PaymentCommand;
import com.ars.payment.service.paymentProcess.model.PaymentProcessResult;
import org.springframework.stereotype.Component;

@Component
public class ThirdPartyOnlyPaymentStrategy implements PaymentProcessingStrategy {
    @Override
    public PaymentStrategy key() {
        return PaymentStrategy.THIRD_PARTY_ONLY;
    }

    @Override
    public PaymentProcessResult process(PaymentCommand command) {
        // TODO: 3rd party provider entegrasyonu burada implement edilecek.
        return new PaymentProcessResult(true, "THIRD_PARTY_ONLY placeholder: odeme akisi henuz implement edilmedi.");
    }
}

