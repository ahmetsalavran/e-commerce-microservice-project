package com.ars.order.repositories;

import com.ars.order.models.entities.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
        Optional<OrderItem> findFirstByOrder_OrderIdAndProductId(Long orderId, Long productId);
        List<OrderItem> findAllByOrder_OrderId(Long orderId);

}
