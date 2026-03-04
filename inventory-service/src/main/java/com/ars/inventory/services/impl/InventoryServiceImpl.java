package com.ars.inventory.services.impl;

import com.ars.core.infrastructure.idempotency.repo.ProcessedEventRepository;
import com.ars.core.infrastructure.outbox.entity.OutboxEvent;
import com.ars.core.infrastructure.outbox.messaging.OutboxCreatedEvent;
import com.ars.core.infrastructure.outbox.service.OutboxEventService;
import com.ars.core.infrastructure.tx.AfterCommitExecutor;
import com.ars.core.infrastructure.web.error.InternalServerException;
import com.ars.contract.catalog.GetProductPricesRequest;
import com.ars.contract.messaging.events.OrderConfirmedEvent;
import com.ars.contract.messaging.events.OrderItemDto;
import com.ars.contract.strategy.InventoryStrategy;
import com.ars.inventory.client.CatalogClient;
import com.ars.inventory.messaging.model.InventoryEventType;
import com.ars.inventory.services.InventoryService;
import com.ars.inventory.services.inventoryCheck.InventoryStrategyDispatcher;
import com.ars.inventory.services.inventoryCheck.model.DeductResult;
import com.ars.inventory.services.inventoryCheck.model.StrategyCommand;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ars.core.infrastructure.idempotency.annotation.Idempotent;
import com.ars.core.infrastructure.idempotency.context.IdempotencyContext;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

  private final InventoryStrategyDispatcher dispatcher;
  private final ProcessedEventRepository processedRepo;
  private final OutboxEventService outboxEventService;
  private final ObjectMapper objectMapper;
  private final ApplicationEventPublisher eventPublisher;
  private final CatalogClient catalogClient;

  @Override
  @Idempotent(
          key = "#p0.eventId()",
          eventType="'OrderConfirmedEvent'",
          orderId = "#p0.orderId()"
  )
  @Transactional
  public void handle(OrderConfirmedEvent event, Runnable ackAfterCommit) {
    if (IdempotencyContext.isDuplicate()) {
      log.info("Tekrar eden event atlandı. eventId={} orderId={}", event.eventId(), event.orderId());

      if (ackAfterCommit != null) {
        AfterCommitExecutor.run(ackAfterCommit);
      }
      return;
    }

    InventoryStrategy key = InventoryStrategy.valueOf(event.orderType());

    StrategyCommand cmd = new StrategyCommand(
            event.eventId(),
            event.orderId(),
            event.items().stream()
                    .map(i -> new StrategyCommand.Item(i.productId(), i.qty()))
                    .toList()
    );

    DeductResult result = dispatcher.dispatch(key, cmd);
    BigDecimal calculatedTotal = result.success() ? calculateTotalFromListing(event.items()) : BigDecimal.ZERO;

    String eventType = result.success()
            ? InventoryEventType.INVENTORY_CONFIRMED.name()
            : InventoryEventType.INVENTORY_REJECTED.name();

    OutboxEvent saved = outboxEventService.createAndSave(
            "INVENTORY",
            event.eventId(),
            eventType,
            String.valueOf(event.orderId()),
            event.orderType(),
            toJson(result)
    );
    if (result.success()) {
      processedRepo.updateStatus(event.eventId(), "DONE");
      log.info("Event başarıyla işlendi. eventId={} orderId={} kural={} totalPrice={}",
              event.eventId(), event.orderId(), key, calculatedTotal);
    } else {
      processedRepo.updateStatus(event.eventId(), "REJECTED - LACK OF QUANTITY");
      log.warn("Event reddedildi. eventId={} orderId={} kural={} neden={}",
              event.eventId(), event.orderId(), key, result.message());
    }

    publishOutboxAndMaybeAckAfterCommit(saved, ackAfterCommit);
  }

  private void publishOutboxAndMaybeAckAfterCommit(OutboxEvent saved, Runnable ackAfterCommit) {
    AfterCommitExecutor.run(() -> {
      try {
        eventPublisher.publishEvent(new OutboxCreatedEvent(
                saved.getId(),
                saved.getEventType(),
                saved.getKey(),
                saved.getPayload()
        ));
      } catch (Exception e) {
        log.error("AFTER_COMMIT publish başarısız. outboxId={} eventType={}", saved.getId(), saved.getEventType(), e);
      } finally {
        if (ackAfterCommit != null) ackAfterCommit.run();   // ✅ null-safe
      }
    });
  }
  private String toJson(Object o) {
    try {
      return objectMapper.writeValueAsString(o);
    } catch (JsonProcessingException e) {
      throw new InternalServerException("Outbox payload serileştirilemedi.", e);
    }
  }

  private BigDecimal calculateTotalFromListing(List<OrderItemDto> items) {
    List<Long> productIds = items.stream()
            .map(OrderItemDto::productId)
            .distinct()
            .toList();

    Map<Long, BigDecimal> priceMap = catalogClient.getProductPrices(new GetProductPricesRequest(productIds));

    BigDecimal total = BigDecimal.ZERO;
    for (OrderItemDto item : items) {
      BigDecimal unitPrice = priceMap.get(item.productId());
      if (unitPrice == null) {
        throw new InternalServerException("Inventory toplamı hesaplanamadı. Ürün fiyatı bulunamadı. productId=" + item.productId());
      }
      total = total.add(unitPrice.multiply(BigDecimal.valueOf(item.qty())));
    }
    return total;
  }

}
