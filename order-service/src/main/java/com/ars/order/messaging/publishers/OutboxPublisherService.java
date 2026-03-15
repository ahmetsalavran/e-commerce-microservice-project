package com.ars.order.messaging.publishers;

import com.ars.contract.messaging.Topics;
import com.ms.core.infrastructure.outbox.messaging.OutboxJob;
import com.ms.core.infrastructure.outbox.repo.OutboxEventRepository;
import com.ms.core.infrastructure.outbox.runtime.OutboxJobPublisher;
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
    private static final String EVENT_TYPE_ORDER_CONFIRMED = "ORDER_CONFIRMED";
    private static final String EVENT_TYPE_ORDER_CONFIRMED_PARTITIONED = "ORDER_CONFIRMED_PARTITIONED";

    private final OutboxEventRepository repo;
    private final KafkaTemplate<String, String> kafkaTemplate;
    @Value("${app.topics.order-confirmed:" + Topics.ORDER_CONFIRMED + "}")
    private String orderConfirmedTopic;
    @Value("${app.topics.order-confirmed-partitioned:" + Topics.ORDER_CONFIRMED_PARTITIONED + "}")
    private String orderConfirmedPartitionedTopic;
    private Map<String, String> topicByEventType;

    @PostConstruct
    void initTopics() {
        this.topicByEventType = Map.of(
                EVENT_TYPE_ORDER_CONFIRMED, orderConfirmedTopic,
                EVENT_TYPE_ORDER_CONFIRMED_PARTITIONED, orderConfirmedPartitionedTopic
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void publishFastPath(OutboxJob job) {
        try {
            if (EVENT_TYPE_ORDER_CONFIRMED_PARTITIONED.equals(job.eventType())) {
                kafkaTemplate
                        .send(orderConfirmedPartitionedTopic, partitionOf(job.keyId()), job.keyId(), job.payload())
                        .get();
            } else {
                kafkaTemplate
                        .send(topicByEventType.get(job.eventType()), job.keyId(), job.payload())
                        .get();
            }

            repo.markSent(job.outboxRowId());
        } catch (Exception ex) {
            repo.markFailed(job.outboxRowId());
            throw new RuntimeException(ex);
        }
    }

    private int partitionOf(String key) {
        int idx = key.lastIndexOf(':');
        if (idx < 0 || idx == key.length() - 1) {
            throw new IllegalArgumentException("Parçalı event key değeri ':<partition>' ile bitmeli. key=" + key);
        }
        return Integer.parseInt(key.substring(idx + 1));
    }
}
