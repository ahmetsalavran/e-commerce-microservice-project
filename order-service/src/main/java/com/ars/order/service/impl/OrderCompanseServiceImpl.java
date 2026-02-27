package com.ars.order.service.impl;

import com.ars.core.infrastructure.idempotency.annotation.Idempotent;
import com.ars.core.infrastructure.idempotency.context.IdempotencyContext;
import com.ars.core.infrastructure.idempotency.repo.ProcessedEventRepository;
import com.ars.order.models.entities.OrdersCart;
import com.ars.order.models.entities.OrderStatus;
import com.ars.order.models.eventModels.InventoryRejectedEvent;
import com.ars.order.repositories.OrderRepository;
import com.ars.order.service.OrderCompanseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCompanseServiceImpl implements OrderCompanseService {

    private final OrderRepository orderRepository;
    private final ProcessedEventRepository processedEventRepository;

    @Transactional
    @Idempotent(
            key = "#p0.eventId()",
            eventType="'InventoryRejectedEvent'",
            orderId = "#p0.orderId()"
    )
    public void onInventoryRejected(InventoryRejectedEvent event, Runnable ackAfterCommit) {

        if (IdempotencyContext.isDuplicate()) {
            log.info("Duplicate event skipped. eventId={} orderId={}", event.eventId(), event.orderId());

            if (ackAfterCommit != null) {
                TransactionSynchronizationManager.registerSynchronization(afterCommit(ackAfterCommit));
            }
            return;
        }

        OrdersCart order = orderRepository.findById(event.orderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found " + event.orderId()));

        if (order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.REJECTED);
            //order.setCancelReason(event.message());
            orderRepository.save(order);
        }
        log.warn("Order rejected by inventory. orderId={} reason={}", event.orderId(), event.message());
    }

    private static TransactionSynchronization afterCommit(Runnable r) {
        return new TransactionSynchronization() {
            @Override public void afterCommit() { r.run(); }
        };
    }
}