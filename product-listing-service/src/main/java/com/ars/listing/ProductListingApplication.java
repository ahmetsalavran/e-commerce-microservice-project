package com.ars.listing;

import com.ars.listing.entity.Product;
import com.ars.listing.repositories.ProductRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackageClasses = Product.class)
@EnableJpaRepositories(basePackageClasses = ProductRepository.class)
public class ProductListingApplication {
  public static void main(String[] args) {
    SpringApplication.run(ProductListingApplication.class, args);
  }
}
