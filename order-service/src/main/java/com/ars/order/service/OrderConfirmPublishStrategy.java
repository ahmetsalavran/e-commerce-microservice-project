package com.ars.order.service;

import com.ars.order.models.entities.OrdersCart;
import com.ars.contract.messaging.events.OrderItemDto;

import java.util.List;

public interface OrderConfirmPublishStrategy {
    void publish(OrdersCart order, List<OrderItemDto> items);
}
