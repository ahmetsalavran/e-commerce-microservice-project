package com.ars.order.messaging.outer;

import com.ars.order.repositories.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class OutboxPublisherService {

    private final OutboxEventRepository repo;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void publishFastPath(OutboxJob job) {
        try {
            kafkaTemplate
                    .send(topicOf(job.eventType()), job.keyId(), job.payload())
                    .get(); // ✅

            repo.markSent(job.outboxRowId());
        } catch (Exception ex) {
            repo.markFailed(job.outboxRowId());
            throw new RuntimeException(ex);
        }
    }


    private String topicOf(String eventType) {
        return switch (eventType) {
            case "ORDER_CONFIRMED" -> "order.confirmed";
            default -> throw new IllegalArgumentException("Unknown eventType=" + eventType);
        };
    }
}
