package com.ars.payment.service.impl;

import com.ars.contract.messaging.events.PaymentCompensationRequestedEvent;
import com.ars.contract.messaging.events.PaymentChargeRequestedEvent;
import com.ars.contract.strategy.PaymentStrategy;
import com.ars.core.infrastructure.idempotency.annotation.Idempotent;
import com.ars.core.infrastructure.idempotency.context.IdempotencyContext;
import com.ars.core.infrastructure.tx.AfterCommitExecutor;
import com.ars.payment.entity.PaymentAccountEvent;
import com.ars.payment.messaging.publishers.PaymentCompensationPublisher;
import com.ars.payment.repository.PaymentAccountEventRepository;
import com.ars.payment.service.PaymentService;
import com.ars.payment.service.paymentProcess.PaymentStrategyDispatcher;
import com.ars.payment.service.paymentProcess.model.PaymentCommand;
import com.ars.payment.service.paymentProcess.model.PaymentProcessResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final String EVENT_PAYMENT_TOPUP = "PAYMENT_TOPUP";

    private final PaymentStrategyDispatcher paymentStrategyDispatcher;
    private final PaymentCompensationPublisher paymentCompensationPublisher;
    private final PaymentAccountEventRepository paymentEventRepository;

    @Override
    @Transactional
    @Idempotent(
            key = "#p0.eventId()",
            eventType = "'PaymentChargeRequestedEvent'",
            orderId = "#p0.orderId()"
    )
    public void handle(PaymentChargeRequestedEvent event, Runnable ackAfterCommit) {
        if (IdempotencyContext.isDuplicate()) {
            log.info("Tekrarlanan payment event atlandi. eventId={} orderId={}", event.eventId(), event.orderId());
            AfterCommitExecutor.run(ackAfterCommit);
            return;
        }

        PaymentStrategy paymentStrategy = event.paymentStrategy();
        BigDecimal payableAmount = event.amount() == null ? BigDecimal.ZERO : event.amount().max(BigDecimal.ZERO);

        PaymentCommand command = new PaymentCommand(
                event.eventId(),
                event.orderId(),
                event.customerId(),
                payableAmount
        );

        PaymentProcessResult result = paymentStrategyDispatcher.dispatch(paymentStrategy, command);
        log.info(
                "Payment workflow calisti. orderId={} customerId={} strategy={} success={} message={}",
                event.orderId(),
                event.customerId(),
                paymentStrategy,
                result.success(),
                result.message()
        );

        if (!result.success()) {
            PaymentCompensationRequestedEvent compensationEvent = new PaymentCompensationRequestedEvent(
                    event.eventId(),
                    event.orderId(),
                    event.customerId(),
                    paymentStrategy,
                    payableAmount,
                    result.message(),
                    OffsetDateTime.now()
            );
            paymentCompensationPublisher.publish(compensationEvent);
            log.warn("Payment fail oldugu icin inventory compensation event'i publish edildi. orderId={} strategy={}",
                    event.orderId(), paymentStrategy);
        }

        AfterCommitExecutor.run(ackAfterCommit);
    }

    @Override
    @Transactional
    public BigDecimal topUp(long customerId, BigDecimal amount) {
        BigDecimal normalizedAmount = amount == null ? BigDecimal.ZERO : amount.max(BigDecimal.ZERO);
        if (normalizedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("amount must be greater than zero.");
        }

        List<PaymentAccountEvent> lockedEvents = paymentEventRepository.findByCustomerIdForUpdate(customerId);
        BigDecimal currentBalance = lockedEvents.stream()
                .map(PaymentAccountEvent::getAmount)
                .map(v -> v == null ? BigDecimal.ZERO : v)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal newBalance = currentBalance.add(normalizedAmount);
        paymentEventRepository.save(PaymentAccountEvent.of(
                customerId,
                null,
                "topup-" + customerId + "-" + UUID.randomUUID(),
                EVENT_PAYMENT_TOPUP,
                normalizedAmount
        ));
        return newBalance;
    }
}
