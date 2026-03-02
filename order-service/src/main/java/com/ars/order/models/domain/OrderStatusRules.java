package com.ars.order.models.domain;

import com.ars.order.models.entities.OrderStatus;

public final class OrderStatusRules {
    private OrderStatusRules() {}

    public static void requireCanConfirm(OrderStatus status) {
        if (status != OrderStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT order can be confirmed. status=" + status);
        }
    }

    public static void requireCanCancel(OrderStatus status) {
        if (status == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Order already cancelled");
        }
        if (status == OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Confirmed order cannot be cancelled");
        }
    }
}
