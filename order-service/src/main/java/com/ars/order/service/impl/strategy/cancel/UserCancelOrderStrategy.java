package com.ars.order.service.impl.strategy.cancel;

import com.ars.order.models.entities.OrdersCart;
import com.ars.order.repository.OrderRepository;
import org.springframework.stereotype.Service;

@Service
public class UserCancelOrderStrategy extends AbstractCancelOrderStrategy {

    public UserCancelOrderStrategy(OrderRepository repo) {
        super(repo);
    }

    @Override
    protected void doCancel(OrdersCart order) {
        // user reason specific side effects can be placed here.
    }
}
