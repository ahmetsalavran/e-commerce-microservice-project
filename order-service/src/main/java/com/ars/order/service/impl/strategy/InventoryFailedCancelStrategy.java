package com.ars.order.service.impl.strategy;

import com.ars.order.models.entities.OrderStatus;
import com.ars.order.models.entities.OrdersCart;
import com.ars.order.repositories.OrderRepository;
import org.springframework.stereotype.Service;

@Service
public class InventoryFailedCancelStrategy
        extends AbstractCancelOrderStrategy {

    public InventoryFailedCancelStrategy(OrderRepository repo) {
        super(repo);
    }

    @Override
    protected void doCancel(OrdersCart order) {
        // burada inventory release çağrısı yok
        // çünkü zaten reserve başarısızdı
        order.setStatus(OrderStatus.CANCELLED);
    }
}

