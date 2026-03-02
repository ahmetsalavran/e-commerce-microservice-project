package com.ars.order.service.impl.strategy.cancel;

import com.ars.order.models.entities.OrdersCart;
import com.ars.order.repository.OrderRepository;
import org.springframework.stereotype.Service;

@Service
public class TimeoutCancelOrderStrategy
        extends AbstractCancelOrderStrategy {

    public TimeoutCancelOrderStrategy(OrderRepository repo) {
        super(repo);
    }

    @Override
    protected void doCancel(OrdersCart order) {
        // timeout specific side effects can be placed here.
    }
}
