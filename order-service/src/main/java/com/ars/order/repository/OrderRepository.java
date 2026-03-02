package com.ars.order.repository;

import com.ars.order.models.entities.OrderStatus;
import com.ars.order.models.entities.OrdersCart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<OrdersCart, Long> {
    Optional<OrdersCart> findFirstByCustomerIdAndStatus(Long customerId, OrderStatus orderStatus);
}

