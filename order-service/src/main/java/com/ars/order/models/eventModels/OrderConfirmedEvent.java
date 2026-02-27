package com.ars.order.models.eventModels;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderConfirmedEvent {

    private String eventId;
    private Long orderId;
    private Long customerId;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant occurredAt;

    private String orderType;

    private List<OrderItemDto> items;

    // 🔥 EKLENDİ
    private BigDecimal totalPrice;
}