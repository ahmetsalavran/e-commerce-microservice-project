package com.ars.inventory.services;

import com.ars.inventory.models.OrderConfirmedEvent;

public interface InventoryService {
    void handle(OrderConfirmedEvent e, Runnable ackAfterCommit);
}
