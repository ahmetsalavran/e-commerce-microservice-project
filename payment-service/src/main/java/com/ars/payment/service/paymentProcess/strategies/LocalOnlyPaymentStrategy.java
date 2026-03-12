package com.ars.payment.service.paymentProcess.strategies;

import com.ars.contract.strategy.PaymentStrategy;
import com.ars.payment.entity.PaymentAccountEvent;
import com.ars.payment.repository.PaymentAccountEventRepository;
import com.ars.payment.service.paymentProcess.PaymentProcessingStrategy;
import com.ars.payment.service.paymentProcess.model.PaymentCommand;
import com.ars.payment.service.paymentProcess.model.PaymentProcessResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class LocalOnlyPaymentStrategy implements PaymentProcessingStrategy {

    private static final String EVENT_PAYMENT_CONFIRMED = "PAYMENT_CONFIRMED";

    private final PaymentAccountEventRepository paymentEventRepository;

    @Override
    public PaymentStrategy key() {
        return PaymentStrategy.LOCAL_ONLY;
    }

    @Override
    public PaymentProcessResult process(PaymentCommand command) {
        BigDecimal amount = normalizeAmount(command.totalAmount());
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return new PaymentProcessResult(true, "LOCAL_ONLY amount=0 oldugu icin odeme atlandi.");
        }

        List<PaymentAccountEvent> lockedEvents = paymentEventRepository.findByCustomerIdForUpdate(command.customerId());
        BigDecimal before = lockedEvents.stream()
                .map(PaymentAccountEvent::getAmount)
                .map(this::nullToZero)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal delta = amount.negate();
        BigDecimal after = before.add(delta);
        if (after.compareTo(BigDecimal.ZERO) < 0) {
            return new PaymentProcessResult(false, "Yetersiz bakiye. currentBalance=" + before + " required=" + amount);
        }

        paymentEventRepository.save(PaymentAccountEvent.of(
                command.customerId(),
                command.orderId(),
                command.eventId(),
                EVENT_PAYMENT_CONFIRMED,
                delta
        ));

        return new PaymentProcessResult(true, "Local odeme basarili. balanceBefore=" + before + " balanceAfter=" + after);
    }

    private BigDecimal normalizeAmount(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount.max(BigDecimal.ZERO);
    }

    private BigDecimal nullToZero(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }
}
