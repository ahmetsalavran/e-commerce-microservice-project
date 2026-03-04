package com.ars.contract.messaging.events;

public record OrderItemDto(
        long productId,
        int qty
) {}
