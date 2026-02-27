package com.ars.order.service;

import com.ars.order.models.eventModels.InventoryRejectedEvent;

public interface OrderCompanseService {
    void onInventoryRejected(InventoryRejectedEvent event, Runnable ackAfterCommit);
}
