package com.ars.inventory.services.inventoryCheck;

import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class InventoryStrategyRegistry {

    private final Map<com.ars.contract.strategy.InventoryStrategy, InventoryStrategy> strategies;

    public InventoryStrategyRegistry(List<InventoryStrategy> strategyList) {
        EnumMap<com.ars.contract.strategy.InventoryStrategy, InventoryStrategy> map =
                new EnumMap<>(com.ars.contract.strategy.InventoryStrategy.class);

        for (InventoryStrategy s : strategyList) {
            com.ars.contract.strategy.InventoryStrategy key = s.key();
            InventoryStrategy prev = map.putIfAbsent(key, s);
            if (prev != null) {
                throw new IllegalStateException("Aynı key için birden fazla strategy var. key=" + key
                        + " -> " + prev.getClass().getName() + " ve " + s.getClass().getName());
            }
        }
        this.strategies = Map.copyOf(map);
    }

    public InventoryStrategy getRequired(com.ars.contract.strategy.InventoryStrategy key) {
        InventoryStrategy s = strategies.get(key);
        if (s == null) throw new IllegalArgumentException("Bu key için strategy kayıtlı değil. key=" + key);
        return s;
    }

    public Map<com.ars.contract.strategy.InventoryStrategy, InventoryStrategy> all() {
        return strategies;
    }
}
