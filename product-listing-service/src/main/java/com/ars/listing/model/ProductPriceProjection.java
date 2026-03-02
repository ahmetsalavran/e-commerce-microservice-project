package com.ars.listing.model;

import java.math.BigDecimal;

public interface ProductPriceProjection {
    Long getProductId();
    BigDecimal getBasePrice();
}
