package com.ars.order.service.impl;

import com.ars.contract.catalog.GetProductPricesRequest;
import com.ars.contract.catalog.ProductPriceDto;
import com.ars.order.feignClients.CatalogClient;
import com.ars.order.models.entities.*;
import com.ars.order.models.enums.CancelReason;
import com.ars.order.models.eventModels.OrderConfirmedEvent;
import com.ars.order.models.eventModels.OrderItemDto;
import com.ars.order.models.request.AddToCartRequest;
import com.ars.order.messaging.inner.OutboxCreatedEvent;
import com.ars.order.repositories.OutboxEventRepository;
import com.ars.order.repositories.OrderItemRepository;
import com.ars.order.repositories.OrderRepository;
import com.ars.order.service.CancelOrderStrategy;
import com.ars.order.service.OrderService;
import com.ars.order.service.impl.factory.CancelOrderStrategyFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final CancelOrderStrategyFactory strategyFactory;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;
    private final CatalogClient catalogClient;


    public OrderServiceImpl(OrderRepository orderRepository,
                            OrderItemRepository orderItemRepository,
                            OutboxEventRepository outboxEventRepository,
                            CancelOrderStrategyFactory strategyFactory,
                            ApplicationEventPublisher eventPublisher,
                            ObjectMapper objectMapper,
                            CatalogClient catalogClient) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.strategyFactory = strategyFactory;
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
        this.catalogClient = catalogClient;
    }

    @Override
    @Transactional
    public Long addItem(AddToCartRequest req) {

        OrdersCart draft = orderRepository
                .findFirstByCustomerIdAndStatus(req.getCustomerId(), OrderStatus.DRAFT)
                .orElseGet(() -> {
                    OrdersCart o = new OrdersCart();
                    o.setCustomerId(req.getCustomerId());
                    o.setStatus(OrderStatus.DRAFT);
                    o.setCreatedAt(OffsetDateTime.now());
                    o.setUpdatedAt(OffsetDateTime.now());
                    o.setOrderType(InventoryStrategyKey.ALL_OR_NOTHING);//BUNU KULLANICIDAN AL
                    return orderRepository.save(o);
                });

        OrderItem item = orderItemRepository
                .findFirstByOrder_OrderIdAndProductId(draft.getOrderId(), req.getProductId())
                .orElseGet(() -> {
                    OrderItem oi = new OrderItem();
                    oi.setOrder(draft);
                    oi.setProductId(req.getProductId());
                    oi.setQty(0);
                    oi.setCreatedAt(OffsetDateTime.now());
                    oi.setUpdatedAt(OffsetDateTime.now());
                    return oi;
                });

        item.setQty(item.getQty() + req.getQty());
        item.setUpdatedAt(OffsetDateTime.now());
        orderItemRepository.save(item);

        draft.setUpdatedAt(OffsetDateTime.now());
        orderRepository.save(draft);

        return draft.getOrderId();
    }

    @Override
    @Transactional
    public Boolean cancelCart(Long orderId, CancelReason reason) {
        OrdersCart order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        CancelOrderStrategy strategy = strategyFactory.getStrategy(reason);
        strategy.cancel(order);
        return Boolean.TRUE;
    }

    @Override
    @Transactional
    public void confirmCart(Long orderId) {

        OrdersCart order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Sepet Bulunamadı: " + orderId));

        if (order.getStatus() != OrderStatus.DRAFT) {
            throw new RuntimeException("Sadece DRAFT olan sepetler onaylanabilir. status=" + order.getStatus());
        }

        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new RuntimeException("Sepetiniz Boş.");
        }

        List<Long> productIds = order.getItems().stream()
                .map(OrderItem::getProductId)
                .distinct()
                .toList();

        GetProductPricesRequest request = new GetProductPricesRequest(productIds);

        List<ProductPriceDto> prices = catalogClient.getProductPrices(request);

        Map<Long, BigDecimal> priceMap = prices.stream()
                .collect(Collectors.toMap(
                        ProductPriceDto::productId,
                        ProductPriceDto::basePrice
                ));

        // 2️⃣ Total hesapla
        BigDecimal total = BigDecimal.ZERO;

        for (OrderItem item : order.getItems()) {

            BigDecimal unitPrice = priceMap.get(item.getProductId());

            if (unitPrice == null) {
                throw new IllegalStateException("Price not found for productId=" + item.getProductId());
            }

            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(item.getQty()));
            total = total.add(lineTotal);
        }

        order.setTotalPrice(total);
        order.setStatus(OrderStatus.PENDING);
        order.setUpdatedAt(OffsetDateTime.now());

        orderRepository.saveAndFlush(order);

        // 4️⃣ Event üret
        List<OrderItemDto> items = order.getItems().stream()
                .map(i -> new OrderItemDto(i.getProductId(), i.getQty()))
                .toList();

        try {
            OrderConfirmedEvent event = new OrderConfirmedEvent(
                    UUID.randomUUID().toString(),
                    order.getOrderId(),
                    order.getCustomerId(),
                    Instant.now(),
                    order.getOrderType().toString(),
                    items,
                    total
            );

            String payload = objectMapper.writeValueAsString(event);

            OutboxEvent outbox = new OutboxEvent();
            outbox.setAggregateType("ORDER");
            outbox.setAggregateId(String.valueOf(order.getOrderId()));
            outbox.setEventType("ORDER_CONFIRMED");
            outbox.setKey(String.valueOf(order.getOrderId()));
            outbox.setOrderType(String.valueOf(order.getOrderType()));
            outbox.setStatus("NEW");
            outbox.setRetries(0);
            outbox.setCreatedAt(OffsetDateTime.now());
            outbox.setAvailableAt(OffsetDateTime.now());
            outbox.setPayload(payload);

            OutboxEvent saved = outboxEventRepository.save(outbox);

            eventPublisher.publishEvent(new OutboxCreatedEvent(
                    saved.getId(),
                    saved.getEventType(),
                    saved.getKey(),
                    saved.getPayload()
            ));

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize OrderConfirmedEvent", e);
        }
    }

}
