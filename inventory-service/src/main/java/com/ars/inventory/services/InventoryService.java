package com.ars.inventory.services;

import com.ars.contract.messaging.events.OrderConfirmedEvent;

public interface InventoryService {
    void handle(OrderConfirmedEvent e, Runnable ackAfterCommit);
}
