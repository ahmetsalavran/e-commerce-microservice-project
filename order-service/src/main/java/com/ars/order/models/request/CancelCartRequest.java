package com.ars.order.models.request;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Data
public class CancelCartRequest {
    @NotNull
    @Positive
    private Long orderId;
}
