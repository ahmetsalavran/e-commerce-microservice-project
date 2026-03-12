package com.ars.inventory.repository;

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

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update ProductStock s
           set s.available = s.available - :qty,
               s.version = s.version + 1,
               s.updatedAt = CURRENT_TIMESTAMP
         where s.productId = :productId
           and s.available >= :qty
           and s.version = :expectedVersion
    """)
    int tryDeductWithVersion(@Param("productId") long productId,
                             @Param("qty") int qty,
                             @Param("expectedVersion") long expectedVersion);

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

    @Query("""
        select s.productId
          from ProductStock s
         where s.productId in :ids
           and s.available = 0
    """)
    List<Long> findZeroAvailableProductIds(@Param("ids") List<Long> ids);
}
