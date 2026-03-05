package com.ars.order.service.impl;

import com.ars.core.infrastructure.idempotency.annotation.Idempotent;
import com.ars.core.infrastructure.idempotency.context.IdempotencyContext;
import com.ars.core.infrastructure.tx.AfterCommitExecutor;
import com.ars.core.infrastructure.web.error.NotFoundException;
import com.ars.contract.messaging.events.InventoryRejectedEvent;
import com.ars.contract.strategy.InventoryStrategy;
import com.ars.order.models.entities.OrderItem;
import com.ars.order.models.entities.OrderStatus;
import com.ars.order.models.entities.OrdersCart;
import com.ars.order.repository.OrderItemRepository;
import com.ars.order.repository.OrderRepository;
import com.ars.order.service.OrderCompensateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCompensateServiceImpl implements OrderCompensateService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Transactional
    @Idempotent(
            key = "#p0.eventId()",
            eventType="'InventoryRejectedEvent'",
            orderId = "#p0.orderId()"
    )
    @Override
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
        if (isPartitioned(event.strategyKey()) && event.items() != null && !event.items().isEmpty()) {
            updateOrderItemsForPartitionedReject(event.orderId(), event.items());
        }
        log.warn("Sipariş envanter tarafından reddedildi. orderId={} neden={}", event.orderId(), event.message());

        AfterCommitExecutor.run(ackAfterCommit);
    }

    private boolean isPartitioned(String strategyKey) {
        return InventoryStrategy.PARTITIONED_BEST_EFFORT.name().equals(strategyKey);
    }

    private void updateOrderItemsForPartitionedReject(long orderId, List<InventoryRejectedEvent.Item> rejectedItems) {
        List<OrderItem> orderItems = orderItemRepository.findAllByOrder_OrderId(orderId);
        Map<Long, OrderItem> byProductId = orderItems.stream()
                .collect(Collectors.toMap(OrderItem::getProductId, Function.identity(), (a, b) -> a));

        for (InventoryRejectedEvent.Item rejectedItem : rejectedItems) {
            OrderItem orderItem = byProductId.get(rejectedItem.productId());
            if (orderItem != null) {
                orderItem.setQty(rejectedItem.deductedQty());
            }
        }
        orderItemRepository.saveAll(orderItems);
    }
}
