package com.ars.listing.repositories;

import com.ars.listing.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("""
           select p.productId as productId,
                  p.basePrice as basePrice
           from Product p
           where p.productId in :ids
           """)
    List<ProductPriceProjection> findPricesByProductIdIn(@Param("ids") List<Long> ids);
}
