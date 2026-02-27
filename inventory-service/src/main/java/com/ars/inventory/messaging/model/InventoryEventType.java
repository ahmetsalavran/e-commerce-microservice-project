package com.ars.inventory.messaging.model;

public enum InventoryEventType {

    INVENTORY_CONFIRMED("inventory.confirmed"),
    INVENTORY_REJECTED("inventory.rejected");

    private final String topic;

    InventoryEventType(String topic) {
        this.topic = topic;
    }

    public String topic() {
        return topic;
    }
}