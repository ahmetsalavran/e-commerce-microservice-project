package com.ars.order.service;

import com.ars.order.models.entities.OrdersCart;

public interface CancelOrderStrategy {
    void cancel(OrdersCart order);
}