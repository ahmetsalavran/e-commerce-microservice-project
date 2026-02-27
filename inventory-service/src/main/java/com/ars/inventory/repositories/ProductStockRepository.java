package com.ars.inventory.repositories;

import com.ars.inventory.models.entities.ProductStock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductStockRepository extends JpaRepository<ProductStock, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update ProductStock s
           set s.available = s.available - :qty,
               s.updatedAt = CURRENT_TIMESTAMP
         where s.productId = :productId
           and s.available >= :qty
    """)
    int tryDeduct(@Param("productId") long productId,
                  @Param("qty") int qty);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from ProductStock s where s.productId in :ids")
    List<ProductStock> lockByProductIdIn(@Param("ids") List<Long> ids);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update ProductStock s
           set s.available = s.available + :qty,
               s.updatedAt = CURRENT_TIMESTAMP
         where s.productId = :productId
    """)
    int restore(@Param("productId") long productId,
                @Param("qty") int qty);
}