package com.ars.payment.api;

import com.ars.payment.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@RestController
@RequestMapping("/payments")
public class PaymentBalanceController {

    private final PaymentService paymentService;

    public PaymentBalanceController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/topups")
    public TopUpResponse topUp(@RequestBody TopUpRequest request) {
        if (request == null || request.customerId() == null || request.customerId() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "customerId must be a positive number.");
        }
        if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "amount must be greater than zero.");
        }
        BigDecimal newBalance;
        try {
            newBalance = paymentService.topUp(request.customerId(), request.amount());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
        return new TopUpResponse(request.customerId(), request.amount(), newBalance);
    }

    public record TopUpRequest(Long customerId, BigDecimal amount) {}

    public record TopUpResponse(Long customerId, BigDecimal topupAmount, BigDecimal balanceAfter) {}
}
