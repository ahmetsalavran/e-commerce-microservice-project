package com.ars.payment.repository;

import com.ars.payment.entity.PaymentAccountEvent;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

public interface PaymentAccountEventRepository extends JpaRepository<PaymentAccountEvent, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from PaymentAccountEvent e where e.customerId = :customerId")
    List<PaymentAccountEvent> findByCustomerIdForUpdate(@Param("customerId") Long customerId);

    @Transactional
    @Modifying
    @Query(value = """
            insert into payment_account_event(
                customer_id, order_id, correlation_id, event_type, amount, created_at
            )
            select
                x.customer_id,
                null,
                'carry-forward-' || x.customer_id || '-' || to_char((now() at time zone 'UTC'), 'YYYYMMDD'),
                'PAYMENT_CARRY_FORWARD',
                x.total_amount,
                now()
            from (
                select
                    a.customer_id,
                    coalesce(sum(a.amount), 0::numeric) as total_amount
                from payment_account_event_archive a
                where a.created_at < :cutoff
                group by a.customer_id
            ) x
            where not exists (
                select 1
                  from payment_account_event e
                 where e.customer_id = x.customer_id
            )
            """, nativeQuery = true)
    int insertCarryForwardForCustomersWithoutActiveEvents(@Param("cutoff") OffsetDateTime cutoff);

    @Transactional
    @Modifying
    @Query(value = """
            with moved as (
                delete from payment_account_event
                 where id in (
                     select id
                       from payment_account_event
                      where created_at < :cutoff
                      order by created_at, id
                      limit :batchSize
                 )
                 returning id, customer_id, order_id, correlation_id, event_type, amount, created_at
            )
            insert into payment_account_event_archive (
                original_id, customer_id, order_id, correlation_id, event_type, amount, created_at, archived_at
            )
            select m.id, m.customer_id, m.order_id, m.correlation_id, m.event_type, m.amount, m.created_at, now()
              from moved m
            """, nativeQuery = true)
    int archiveAndDeleteBatchOlderThan(@Param("cutoff") OffsetDateTime cutoff,
                                       @Param("batchSize") int batchSize);
}
