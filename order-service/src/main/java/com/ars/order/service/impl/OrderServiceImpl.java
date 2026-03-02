package com.ars.order.service.impl;

import com.ars.contract.catalog.GetProductPricesRequest;
import com.ars.contract.messaging.events.OrderItemDto;
import com.ars.core.infrastructure.web.error.BadRequestException;
import com.ars.core.infrastructure.web.error.NotFoundException;
import com.ars.order.client.CatalogClient;
import com.ars.order.client.UserClient;
import com.ars.order.models.entities.*;
import com.ars.order.models.enums.CancelReason;
import com.ars.order.models.request.AddToCartRequest;
import com.ars.order.repository.OrderItemRepository;
import com.ars.order.repository.OrderRepository;
import com.ars.order.service.CancelOrderStrategy;
import com.ars.order.service.OrderService;
import com.ars.order.service.impl.factory.CancelOrderStrategyFactory;
import com.ars.order.service.impl.factory.OrderConfirmPublishStrategyFactory;
import com.ars.order.models.domain.OrderStatusRules;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CancelOrderStrategyFactory strategyFactory;
    private final OrderConfirmPublishStrategyFactory confirmPublishStrategyFactory;
    private final CatalogClient catalogClient;
    private final UserClient userClient;


    public OrderServiceImpl(OrderRepository orderRepository,
                            OrderItemRepository orderItemRepository,
                            CancelOrderStrategyFactory strategyFactory,
                            OrderConfirmPublishStrategyFactory confirmPublishStrategyFactory,
                            CatalogClient catalogClient,
                            UserClient userClient) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.strategyFactory = strategyFactory;
        this.confirmPublishStrategyFactory = confirmPublishStrategyFactory;
        this.catalogClient = catalogClient;
        this.userClient = userClient;
    }

    @Override
    @Transactional
    public Long addItem(AddToCartRequest req) {
        OrdersCart draft = orderRepository
                .findFirstByCustomerIdAndStatus(req.getCustomerId(), OrderStatus.DRAFT)
                .orElseGet(() -> {
                    var strategyPreference = userClient.getStrategiesByCustomer(req.getCustomerId());
                    OrdersCart o = new OrdersCart();
                    o.setCustomerId(req.getCustomerId());
                    o.setStatus(OrderStatus.DRAFT);
                    o.setCreatedAt(OffsetDateTime.now());
                    o.setUpdatedAt(OffsetDateTime.now());
                    o.setOrderType(strategyPreference.inventoryStrategy());
                    o.setPaymentStrategy(strategyPreference.paymentStrategy());
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
                .orElseThrow(() -> new NotFoundException("Order not found"));

        CancelOrderStrategy strategy = strategyFactory.getStrategy(reason);
        strategy.cancel(order);
        return Boolean.TRUE;
    }

    @Override
    @Transactional
    public void confirmCart(Long orderId) {

        OrdersCart order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found. id=" + orderId));

        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

        OrderStatusRules.requireCanConfirm(order.getStatus());

        List<Long> productIds = order.getItems().stream()
                .map(OrderItem::getProductId)
                .distinct()
                .toList();

        GetProductPricesRequest request = new GetProductPricesRequest(productIds);


        Map<Long, BigDecimal> priceMap = catalogClient.getProductPrices(request);

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

        List<OrderItemDto> items = order.getItems().stream()
                .map(i -> new OrderItemDto(i.getProductId(), i.getQty()))
                .toList();

        confirmPublishStrategyFactory
                .getRequired(order.getOrderType())
                .publish(order, items, total);
    }
}
