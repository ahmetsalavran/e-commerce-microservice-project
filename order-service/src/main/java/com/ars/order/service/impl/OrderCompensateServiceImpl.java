package com.ars.order.service.impl;

import com.ars.core.infrastructure.idempotency.annotation.Idempotent;
import com.ars.core.infrastructure.idempotency.context.IdempotencyContext;
import com.ars.core.infrastructure.idempotency.repo.ProcessedEventRepository;
import com.ars.core.infrastructure.tx.AfterCommitExecutor;
import com.ars.core.infrastructure.web.error.NotFoundException;
import com.ars.contract.messaging.events.InventoryRejectedEvent;
import com.ars.order.models.entities.OrderStatus;
import com.ars.order.models.entities.OrdersCart;
import com.ars.order.repository.OrderRepository;
import com.ars.order.service.OrderCompensateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCompensateServiceImpl implements OrderCompensateService {

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
            log.info("Mükerrer event atlandı. eventId={} orderId={}", event.eventId(), event.orderId());
            AfterCommitExecutor.run(ackAfterCommit);
            return;
        }

        OrdersCart order = orderRepository.findById(event.orderId())
                .orElseThrow(() -> new NotFoundException("Sipariş bulunamadı. id=" + event.orderId()));

        if (order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.REJECTED);
            orderRepository.save(order);
        }
        log.warn("Sipariş envanter tarafından reddedildi. orderId={} neden={}", event.orderId(), event.message());

        AfterCommitExecutor.run(ackAfterCommit);
    }
}
