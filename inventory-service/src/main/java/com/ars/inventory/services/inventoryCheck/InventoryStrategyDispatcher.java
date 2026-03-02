package com.ars.inventory.services.inventoryCheck;

import com.ars.inventory.services.inventoryCheck.model.DeductResult;
import com.ars.inventory.services.inventoryCheck.model.StrategyCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryStrategyDispatcher {

    private final InventoryStrategyRegistry registry;

    public DeductResult dispatch(com.ars.contract.strategy.InventoryStrategy key, StrategyCommand cmd) {
        return registry.getRequired(key).deduct(cmd);
    }
}
