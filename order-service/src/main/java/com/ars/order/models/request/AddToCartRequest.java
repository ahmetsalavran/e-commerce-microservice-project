package com.ars.order.models.request;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Data
public class AddToCartRequest {
    @NotNull
    @Positive
    private Long customerId;

    @NotNull
    @Positive
    private Long productId;

    @NotNull
    @Positive
    private Integer qty;

    private String orderType;
}
