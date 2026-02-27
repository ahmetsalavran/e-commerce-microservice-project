package com.ars.order.models.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class AddCartItemRequest {

    /**
     * Sepeti kime ait? (Customer identifier)
     */
    @NotBlank
    private Long customerId;

    /**
     * Eklenen ürünün id'si
     */
    @NotNull
    private Long productId;

    /**
     * Eklenen ürün adedi
     */
    @NotNull
    @Positive
    private Integer qty;
}

