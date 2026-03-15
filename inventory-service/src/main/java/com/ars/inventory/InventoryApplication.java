package com.ars.inventory;

import com.ms.core.infrastructure.idempotency.entity.ProcessedEvent;
import com.ms.core.infrastructure.idempotency.repo.ProcessedEventRepository;
import com.ms.core.infrastructure.outbox.entity.OutboxEvent;
import com.ms.core.infrastructure.outbox.repo.OutboxEventRepository;
import com.ars.inventory.models.entities.ProductStock;
import com.ars.inventory.repository.ProductStockRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableFeignClients
@EnableJpaRepositories(basePackageClasses = {
        ProductStockRepository.class,
        OutboxEventRepository.class,
        ProcessedEventRepository.class
})
@EntityScan(basePackageClasses = {
        ProductStock.class,
        OutboxEvent.class,
        ProcessedEvent.class
})
public class InventoryApplication {
  public static void main(String[] args) {
    SpringApplication.run(InventoryApplication.class, args);
  }
}
