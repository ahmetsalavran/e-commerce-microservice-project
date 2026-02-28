package com.ars.contract.catalog;

import java.math.BigDecimal;

public interface ProductPriceProjection {
    Long getProductId();
    BigDecimal getBasePrice();
}