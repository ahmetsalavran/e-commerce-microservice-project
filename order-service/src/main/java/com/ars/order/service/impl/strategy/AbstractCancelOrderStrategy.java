package com.ars.order.service.impl.strategy;

import com.ars.order.models.entities.OrderStatus;
import com.ars.order.models.entities.OrdersCart;
import com.ars.order.repositories.OrderRepository;
import com.ars.order.service.CancelOrderStrategy;

import java.time.OffsetDateTime;

public abstract class AbstractCancelOrderStrategy
        implements CancelOrderStrategy {

    protected final OrderRepository orderRepository;

    protected AbstractCancelOrderStrategy(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public final void cancel(OrdersCart order) {
        validate(order);
        doCancel(order);
        afterCancel(order);
    }

    protected void validate(OrdersCart order) {
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Order already cancelled");
        }
        if (order.getStatus() == OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Confirmed order cannot be cancelled");
        }
        if (order.getStatus() == OrderStatus.PENDING) {
            // Saga içindeyken cancel edilebilir
            return;
        }
    }

    protected abstract void doCancel(OrdersCart order);

    protected void afterCancel(OrdersCart order) {
        order.setUpdatedAt(OffsetDateTime.now());
        orderRepository.save(order);
    }
}

