package com.ars.contract.messaging;

public final class Topics {
    private Topics() {}

    public static final String ORDER_CONFIRMED = "order.confirmed";
    public static final String ORDER_CONFIRMED_PARTITIONED = "order.confirmed.partitioned";
    public static final String INVENTORY_CONFIRMED = "inventory.confirmed";
    public static final String INVENTORY_REJECTED = "inventory.rejected";
    public static final String PAYMENT_CHARGE_REQUESTED = "payment.charge.requested";
    public static final String INVENTORY_COMPENSATE_REQUESTED = "inventory.compensate.requested";
}
