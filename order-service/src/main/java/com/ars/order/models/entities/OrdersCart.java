package com.ars.order.models.entities;

import com.ars.contract.strategy.InventoryStrategy;
import com.ars.contract.strategy.PaymentStrategy;
import com.ars.contract.core.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@Entity
@Table(name = "orders")
@NoArgsConstructor
public class OrdersCart extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    /*
    @Column(name = "cancel_reason", nullable = false)
    private String  cancelReason;
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private InventoryStrategy orderType;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_strategy", nullable = false, length = 40)
    private PaymentStrategy paymentStrategy;

    @OneToMany(
            mappedBy = "order",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL
    )
    private List<OrderItem> items = new ArrayList<>();

    @Column(name="total_price", precision=19, scale=2, nullable=false)
    private BigDecimal totalPrice = BigDecimal.ZERO;

}
