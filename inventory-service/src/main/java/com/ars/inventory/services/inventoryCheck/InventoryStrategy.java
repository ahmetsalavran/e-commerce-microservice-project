package com.ars.inventory.services.inventoryCheck;

import com.ars.inventory.services.inventoryCheck.model.DeductResult;
import com.ars.inventory.services.inventoryCheck.model.InventoryStrategyKey;
import com.ars.inventory.services.inventoryCheck.model.StrategyCommand;

public interface InventoryStrategy {
    InventoryStrategyKey key();
    DeductResult deduct(StrategyCommand cmd);
}
