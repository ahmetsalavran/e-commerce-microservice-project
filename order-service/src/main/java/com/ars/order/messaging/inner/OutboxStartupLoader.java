package com.ars.order.messaging.inner;

import com.ars.order.models.entities.OutboxEvent;
import com.ars.order.messaging.outer.OutboxJob;
import com.ars.order.repositories.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OutboxStartupLoader {

    /**
     * App down olduktan sonra tekrar ayağa kalktığında birikmiş(kafkaya işlenememiş olan) outboxları
     * kafka'ya tekrar basar.
     * Kritik Nokta: Idempotency
     * Bu modelde şuna dikkat etmek gerekir:
     * Kafka’ya publish edildi
     * Ama status update edilmeden crash oldu
     * Restart sonrası aynı event tekrar publish edilebilir.
     */

    private final OutboxEventRepository repo;
    private final OutboxQueue queue;

    @EventListener(ApplicationReadyEvent.class)
    public void load() {
        List<OutboxEvent> events =
                repo.findByStatusInOrderByIdAsc(List.of("NEW", "FAILED"));
        events.forEach(event -> queue.enqueue(new OutboxJob(event.getId(), event.getEventType(), event.getKey(), event.getPayload())));
    }
}
