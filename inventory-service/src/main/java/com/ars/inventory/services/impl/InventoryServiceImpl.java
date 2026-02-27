package com.ars.inventory.services.impl;

import com.ars.core.infrastructure.idempotency.repo.ProcessedEventRepository;
import com.ars.inventory.messaging.inner.OutboxCreatedEvent;
import com.ars.inventory.messaging.model.InventoryEventType;
import com.ars.inventory.models.OrderConfirmedEvent;
import com.ars.inventory.models.entities.OutboxEvent;
import com.ars.inventory.repositories.OutboxEventRepository;
import com.ars.inventory.services.InventoryService;
import com.ars.inventory.services.inventoryCheck.InventoryStrategyDispatcher;
import com.ars.inventory.services.inventoryCheck.model.DeductResult;
import com.ars.inventory.services.inventoryCheck.model.InventoryStrategyKey;
import com.ars.inventory.services.inventoryCheck.model.StrategyCommand;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.ars.core.infrastructure.idempotency.annotation.Idempotent;
import com.ars.core.infrastructure.idempotency.context.IdempotencyContext;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

  private final InventoryStrategyDispatcher dispatcher;
  private final ProcessedEventRepository processedRepo;
  private final OutboxEventRepository outboxRepo;
  private final ObjectMapper objectMapper;
  private final ApplicationEventPublisher eventPublisher;

  @Override
  @Idempotent(
          key = "#p0.eventId()",
          eventType="'OrderConfirmedEvent'",
          orderId = "#p0.orderId()"
  )
  @Transactional
  public void handle(OrderConfirmedEvent event, Runnable ackAfterCommit) {

    if (IdempotencyContext.isDuplicate()) {
      log.info("Duplicate event skipped. eventId={} orderId={}", event.eventId(), event.orderId());

      if (ackAfterCommit != null) {
        TransactionSynchronizationManager.registerSynchronization(afterCommit(ackAfterCommit));
      }
      return;
    }

    InventoryStrategyKey key = InventoryStrategyKey.valueOf(event.orderType());

    StrategyCommand cmd = new StrategyCommand(
            event.eventId(),
            event.orderId(),
            event.items().stream()
                    .map(i -> new StrategyCommand.Item(i.productId(), i.qty()))
                    .toList()
    );

    DeductResult result = dispatcher.dispatch(key, cmd);

    OutboxEvent saved;
    if (result.success()) {
      saved = outboxRepo.save(newOutbox(
              event.eventId(),
              event.orderId(),
              event.orderType(),
              InventoryEventType.INVENTORY_CONFIRMED,
              toJson(result)
      ));
      processedRepo.updateStatus(event.eventId(), "DONE");
      log.info("Processed OK eventId={} orderId={} policy={}", event.eventId(), event.orderId(), key);
    } else {
      saved = outboxRepo.save(newOutbox(
              event.eventId(),
              event.orderId(),
              event.orderType(),
              InventoryEventType.INVENTORY_REJECTED,
              toJson(result)
      ));
      processedRepo.updateStatus(event.eventId(), "REJECTED - LACK OF QUANTITY");
      log.warn("Rejected eventId={} orderId={} policy={} reason={}",
              event.eventId(), event.orderId(), key, result.message());
    }

    publishOutboxAndMaybeAckAfterCommit(saved, ackAfterCommit);
  }

  private OutboxEvent newOutbox(String eventId, long orderId, String orderType, InventoryEventType eventType, String payload) {
    OutboxEvent o = new OutboxEvent();
    o.setAggregateType("INVENTORY");
    o.setAggregateId(String.valueOf(eventId));
    o.setEventType(eventType);
    o.setKey(String.valueOf(orderId));
    o.setOrderType(String.valueOf(orderType));
    o.setStatus("NEW");
    o.setRetries(0);
    o.setCreatedAt(OffsetDateTime.now());
    o.setAvailableAt(OffsetDateTime.now());
    o.setPayload(payload);
    return o;
  }

  private void publishOutboxAndMaybeAckAfterCommit(OutboxEvent saved, Runnable ackAfterCommit) {
    TransactionSynchronizationManager.registerSynchronization(afterCommit(() -> {
      try {
        eventPublisher.publishEvent(new OutboxCreatedEvent(
                saved.getId(),
                saved.getEventType(),
                saved.getKey(),
                saved.getPayload()
        ));
      } catch (Exception e) {
        log.error("AFTER_COMMIT publish failed. outboxId={} eventType={}", saved.getId(), saved.getEventType(), e);
      } finally {
        if (ackAfterCommit != null) ackAfterCommit.run();   // ✅ null-safe
      }
    }));
  }
  private String toJson(Object o) {
    try {
      return objectMapper.writeValueAsString(o);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("OUTBOX_PAYLOAD_SERIALIZE_FAIL", e);
    }
  }

  private static TransactionSynchronization afterCommit(Runnable r) {
    return new TransactionSynchronization() {
      @Override public void afterCommit() { r.run(); }
    };
  }
}