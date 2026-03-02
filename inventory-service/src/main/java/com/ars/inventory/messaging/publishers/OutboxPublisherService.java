package com.ars.inventory.messaging.publishers;

import com.ars.contract.messaging.Topics;
import com.ars.core.infrastructure.outbox.messaging.OutboxJob;
import com.ars.core.infrastructure.outbox.repo.OutboxEventRepository;
import com.ars.core.infrastructure.outbox.runtime.OutboxJobPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OutboxPublisherService implements OutboxJobPublisher {
    private final OutboxEventRepository repo;
    private final KafkaTemplate<String, String> kafkaTemplate;
    @Value("${app.topics.inventory-confirmed:" + Topics.INVENTORY_CONFIRMED + "}")
    private String inventoryConfirmedTopic;
    @Value("${app.topics.inventory-rejected:" + Topics.INVENTORY_REJECTED + "}")
    private String inventoryRejectedTopic;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void publishFastPath(OutboxJob job) {
        try {
            kafkaTemplate
                    .send(topicOf(job.eventType()), job.keyId(), job.payload())
                    .get();
            repo.markSent(job.outboxRowId());
        } catch (Exception ex) {
            repo.markFailed(job.outboxRowId());
            throw new RuntimeException(ex);
        }
    }

    private String topicOf(String eventType) {
        return switch (eventType) {
            case "INVENTORY_CONFIRMED" -> inventoryConfirmedTopic;
            case "INVENTORY_REJECTED" -> inventoryRejectedTopic;
            default -> throw new IllegalArgumentException("Unknown eventType=" + eventType);
        };
    }
}
