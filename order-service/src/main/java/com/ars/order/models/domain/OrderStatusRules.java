package com.ars.order.models.domain;

import com.ars.order.models.entities.OrderStatus;

public final class OrderStatusRules {
    private OrderStatusRules() {}

    public static void requireCanConfirm(OrderStatus status) {
        if (status != OrderStatus.DRAFT) {
            throw new IllegalStateException("Sadece DRAFT durumundaki sipariş onaylanabilir. durum=" + status);
        }
    }

    public static void requireCanCancel(OrderStatus status) {
        if (status == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Sipariş zaten iptal edilmiş.");
        }
        if (status == OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Onaylanan sipariş iptal edilemez.");
        }
    }
}
