package com.ars.contract.catalog;

import java.math.BigDecimal;

public record ProductPriceDto(
        Long productId,
        BigDecimal basePrice
) {}
