package com.ars.order.models.request;

import lombok.Data;

@Data
public class CancelCartRequest {
    private Long orderId;
}
