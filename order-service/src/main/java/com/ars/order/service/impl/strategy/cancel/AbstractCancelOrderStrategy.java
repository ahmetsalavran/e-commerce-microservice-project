package com.ars.order.service.impl.strategy.cancel;

import com.ars.order.models.entities.OrderStatus;
import com.ars.order.models.entities.OrdersCart;
import com.ars.order.repository.OrderRepository;
import com.ars.order.service.CancelOrderStrategy;
import com.ars.order.models.domain.OrderStatusRules;

public abstract class AbstractCancelOrderStrategy
        implements CancelOrderStrategy {

    protected final OrderRepository orderRepository;

    protected AbstractCancelOrderStrategy(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public final void cancel(OrdersCart order) {
        OrderStatusRules.requireCanCancel(order.getStatus());
        doCancel(order);
        order.setStatus(OrderStatus.CANCELLED);
        afterCancel(order);
    }

    protected abstract void doCancel(OrdersCart order);

    protected void afterCancel(OrdersCart order) {
        orderRepository.save(order);
    }
}
