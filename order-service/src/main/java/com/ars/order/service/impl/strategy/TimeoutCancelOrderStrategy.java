package com.ars.order.service.impl.strategy;

import com.ars.order.models.entities.OrderStatus;
import com.ars.order.models.entities.OrdersCart;
import com.ars.order.repositories.OrderRepository;
import org.springframework.stereotype.Service;

@Service
public class TimeoutCancelOrderStrategy
        extends AbstractCancelOrderStrategy {

    public TimeoutCancelOrderStrategy(OrderRepository repo) {
        super(repo);
    }

    @Override
    protected void doCancel(OrdersCart order) {
        order.setStatus(OrderStatus.CANCELLED);
    }
}

