package com.ars.listing.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class ProductDto {
    private Long productId;
    private String sku;
    private String name;
    private BigDecimal basePrice;
    private OffsetDateTime updatedAt;
}
