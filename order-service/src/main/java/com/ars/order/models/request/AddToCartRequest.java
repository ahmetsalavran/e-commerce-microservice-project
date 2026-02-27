package com.ars.order.models.request;

import lombok.Data;

@Data
public class AddToCartRequest {
    private Long customerId;
    private Long productId;
    private Integer qty;
    private String orderType;
}
