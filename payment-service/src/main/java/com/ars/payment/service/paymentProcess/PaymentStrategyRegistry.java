package com.ars.payment.service.paymentProcess;

import com.ars.contract.strategy.PaymentStrategy;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PaymentStrategyRegistry {

    private final Map<PaymentStrategy, PaymentProcessingStrategy> strategies;

    public PaymentStrategyRegistry(java.util.List<PaymentProcessingStrategy> strategies) {
        Map<PaymentStrategy, PaymentProcessingStrategy> discovered =
                strategies.stream().collect(Collectors.toMap(PaymentProcessingStrategy::key, Function.identity()));

        Map<PaymentStrategy, PaymentProcessingStrategy> resolved = new EnumMap<>(PaymentStrategy.class);
        resolved.putAll(discovered);

        // ThirdPartyThenLocal strategy intentionally removed for now.
        // Keep the flow alive by routing to THIRD_PARTY_ONLY until a dedicated fallback is implemented.
        if (!resolved.containsKey(PaymentStrategy.THIRD_PARTY_THEN_LOCAL)
                && resolved.containsKey(PaymentStrategy.THIRD_PARTY_ONLY)) {
            resolved.put(PaymentStrategy.THIRD_PARTY_THEN_LOCAL, resolved.get(PaymentStrategy.THIRD_PARTY_ONLY));
        }

        this.strategies = resolved;
    }

    public PaymentProcessingStrategy getRequired(PaymentStrategy strategy) {
        PaymentProcessingStrategy paymentStrategy = strategies.get(strategy);
        if (paymentStrategy == null) {
            throw new IllegalArgumentException("Payment strategy bulunamadi: " + strategy);
        }
        return paymentStrategy;
    }
}
