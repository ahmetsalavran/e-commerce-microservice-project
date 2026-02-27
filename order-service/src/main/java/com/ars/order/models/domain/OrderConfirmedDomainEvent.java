package com.ars.order.models.domain;


public class OrderConfirmedDomainEvent {
    private final Long orderId;
    private final Long customerId;

    public OrderConfirmedDomainEvent(Long orderId,Long customerId) {
        this.customerId = customerId;
        this.orderId = orderId;
    }

    public Long getOrderId() {
        return orderId;
    }
    public Long getCustomerId() {
        return customerId;
    }
}
