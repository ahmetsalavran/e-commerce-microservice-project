package com.ars.order;

import com.ms.core.infrastructure.idempotency.entity.ProcessedEvent;
import com.ms.core.infrastructure.idempotency.repo.ProcessedEventRepository;
import com.ms.core.infrastructure.outbox.entity.OutboxEvent;
import com.ms.core.infrastructure.outbox.repo.OutboxEventRepository;
import com.ars.order.models.entities.OrderItem;
import com.ars.order.models.entities.OrdersCart;
import com.ars.order.repository.OrderItemRepository;
import com.ars.order.repository.OrderRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableFeignClients
@EntityScan(basePackageClasses = {
        OrdersCart.class,
        OrderItem.class,
        OutboxEvent.class,
        ProcessedEvent.class
})
@EnableJpaRepositories(basePackageClasses = {
        OrderRepository.class,
        OrderItemRepository.class,
        OutboxEventRepository.class,
        ProcessedEventRepository.class
})
public class OrderApplication {
  public static void main(String[] args) {
    SpringApplication.run(OrderApplication.class, args);
  }
}
