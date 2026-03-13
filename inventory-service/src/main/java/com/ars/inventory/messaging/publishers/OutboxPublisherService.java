package com.ars.inventory.messaging.publishers;

import com.ars.contract.messaging.Topics;
import com.microservice.infrastructure.outbox.messaging.OutboxJob;
import com.microservice.infrastructure.outbox.repo.OutboxEventRepository;
import com.microservice.infrastructure.outbox.runtime.OutboxJobPublisher;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class OutboxPublisherService implements OutboxJobPublisher {
    private final OutboxEventRepository repo;
    private final KafkaTemplate<String, String> kafkaTemplate;
    @Value("${app.topics.payment-charge-requested:" + Topics.PAYMENT_CHARGE_REQUESTED + "}")
    private String paymentChargeRequestedTopic;
    @Value("${app.topics.inventory-rejected:" + Topics.INVENTORY_REJECTED + "}")
    private String inventoryRejectedTopic;
    private Map<String, String> topicByEventType;

    @PostConstruct
    void initTopics() {
        this.topicByEventType = Map.of(
                "PAYMENT_CHARGE_REQUESTED", paymentChargeRequestedTopic,
                "INVENTORY_REJECTED", inventoryRejectedTopic
        );
    }

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
        String topic = topicByEventType.get(eventType);
        if (topic == null) {
            throw new IllegalArgumentException("Bilinmeyen eventType=" + eventType);
        }
        return topic;
    }
}
