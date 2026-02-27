package com.ars.inventory.services.inventoryCheck.model;

public record StrategyResult(
        boolean accepted,
        String reason
) {}
