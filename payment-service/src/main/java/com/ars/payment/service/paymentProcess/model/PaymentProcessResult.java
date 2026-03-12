package com.ars.payment.service.paymentProcess.model;

public record PaymentProcessResult(
        boolean success,
        String message
) {
}

