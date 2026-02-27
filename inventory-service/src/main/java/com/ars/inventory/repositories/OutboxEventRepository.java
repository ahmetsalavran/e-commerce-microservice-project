package com.ars.inventory.repositories;

import com.ars.inventory.models.entities.OutboxEvent;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {
    List<OutboxEvent> findByStatusInOrderByIdAsc(List<String> statuses);

    @Modifying
    @Query("update OutboxEvent o set o.status='SENT' where o.id = :id")
    void markSent(@Param("id") Long id);

    @Modifying
    @Query("update OutboxEvent o set o.status='FAILED' where o.id = :id")
    void markFailed(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from OutboxEvent o where o.id = :id")
    Optional<OutboxEvent> findByIdForUpdate(@Param("id") Long id);


}
