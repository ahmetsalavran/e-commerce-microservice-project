package com.ars.inventory.services.inventoryCheck;

import com.ars.inventory.services.inventoryCheck.model.DeductResult;
import com.ars.inventory.services.inventoryCheck.model.StrategyCommand;

public interface InventoryStrategy {
    com.ars.contract.strategy.InventoryStrategy key();
    DeductResult deduct(StrategyCommand cmd);
}
