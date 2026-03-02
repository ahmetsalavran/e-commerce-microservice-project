package com.ars.order.service.impl.strategy.cancel;

import com.ars.order.models.entities.OrdersCart;
import com.ars.order.repository.OrderRepository;
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
        // inventory-failed specific side effects can be placed here.
    }
}
