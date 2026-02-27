package com.ars.listing.model.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductCreateRequest {
    private String sku;
    private String name;
    private BigDecimal basePrice;
}


