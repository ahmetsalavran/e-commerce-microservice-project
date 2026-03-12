package com.ars.listing.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class ProductDto {
    private Long productId;
    private String sku;
    private String name;
    private BigDecimal basePrice;
    private Integer available;
    private OffsetDateTime updatedAt;
}
