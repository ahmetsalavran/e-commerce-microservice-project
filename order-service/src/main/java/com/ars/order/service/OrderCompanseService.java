package com.ars.order.service;

import com.ars.contract.messaging.events.InventoryRejectedEvent;

public interface OrderCompanseService {
    void onInventoryRejected(InventoryRejectedEvent event, Runnable ackAfterCommit);
}
