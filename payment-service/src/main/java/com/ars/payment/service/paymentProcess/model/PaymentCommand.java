package com.ars.payment.service.paymentProcess.model;

import java.math.BigDecimal;

public record PaymentCommand(
        String eventId,
        long orderId,
        long customerId,
        BigDecimal totalAmount
) {
}

