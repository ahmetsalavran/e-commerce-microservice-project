package com.ars.listing.repositories;

import java.math.BigDecimal;

public interface ProductPriceProjection {
    Long getProductId();
    BigDecimal getBasePrice();
}
