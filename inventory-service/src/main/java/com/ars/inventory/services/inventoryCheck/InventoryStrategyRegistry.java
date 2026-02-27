package com.ars.inventory.services.inventoryCheck;

import com.ars.inventory.services.inventoryCheck.model.InventoryStrategyKey;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class InventoryStrategyRegistry {

    private final Map<InventoryStrategyKey, InventoryStrategy> strategies;

    public InventoryStrategyRegistry(List<InventoryStrategy> strategyList) {
        EnumMap<InventoryStrategyKey, InventoryStrategy> map = new EnumMap<>(InventoryStrategyKey.class);

        for (InventoryStrategy s : strategyList) {
            InventoryStrategyKey key = s.key();
            InventoryStrategy prev = map.putIfAbsent(key, s);
            if (prev != null) {
                throw new IllegalStateException("Duplicate strategy for key=" + key
                        + " -> " + prev.getClass().getName() + " and " + s.getClass().getName());
            }
        }
        this.strategies = Map.copyOf(map);
    }

    public InventoryStrategy getRequired(InventoryStrategyKey key) {
        InventoryStrategy s = strategies.get(key);
        if (s == null) throw new IllegalArgumentException("No strategy registered for key=" + key);
        return s;
    }

    public Map<InventoryStrategyKey, InventoryStrategy> all() {
        return strategies;
    }
}
