package com.ars.inventory.services.inventoryCheck.strategies;

import com.ars.contract.strategy.InventoryStrategy;
import com.ars.inventory.services.inventoryCheck.model.DeductResult;
import com.ars.inventory.services.inventoryCheck.model.StrategyCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PartitionedBestEffortStrategy implements com.ars.inventory.services.inventoryCheck.InventoryStrategy {

    private final PartialOkButNotZeroStrategy delegate;

    @Override
    public InventoryStrategy key() {
        return InventoryStrategy.PARTITIONED_BEST_EFFORT;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public DeductResult deduct(StrategyCommand command) {
        DeductResult result = delegate.deduct(command);
        return new DeductResult(
                result.eventId(),
                result.orderId(),
                InventoryStrategy.PARTITIONED_BEST_EFFORT.name(),
                result.success(),
                result.message(),
                result.items(),
                result.decidedAt()
        );
    }
}
