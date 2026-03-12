package com.ars.payment.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "payment_account_event")
public class PaymentAccountEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "correlation_id", nullable = false, length = 80)
    private String correlationId;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public static PaymentAccountEvent of(Long customerId,
                                         Long orderId,
                                         String correlationId,
                                         String eventType,
                                         BigDecimal amount) {
        PaymentAccountEvent event = new PaymentAccountEvent();
        event.customerId = customerId;
        event.orderId = orderId;
        event.correlationId = correlationId;
        event.eventType = eventType;
        event.amount = amount;
        event.createdAt = OffsetDateTime.now();
        return event;
    }

    public Long getId() {
        return id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public String getEventType() {
        return eventType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
