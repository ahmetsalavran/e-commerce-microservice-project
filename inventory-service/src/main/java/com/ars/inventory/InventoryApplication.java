package com.ars.inventory;

import com.ms.core.infrastructure.persistence.InfrastructureEntitiesMarker;
import com.ms.core.infrastructure.persistence.InfrastructureRepositoriesMarker;
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
        InfrastructureRepositoriesMarker.class
})
@EntityScan(basePackageClasses = {
        ProductStock.class,
        InfrastructureEntitiesMarker.class
})
public class InventoryApplication {
  public static void main(String[] args) {
    SpringApplication.run(InventoryApplication.class, args);
  }
}
