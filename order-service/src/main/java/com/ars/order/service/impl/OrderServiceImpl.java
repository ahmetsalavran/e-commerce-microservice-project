package com.ars.order.service.impl;

import com.ars.contract.messaging.events.OrderItemDto;
import com.ars.contract.user.UserStrategyPreferenceResponse;
import com.ars.core.infrastructure.web.error.BadRequestException;
import com.ars.core.infrastructure.web.error.NotFoundException;
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
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CancelOrderStrategyFactory strategyFactory;
    private final OrderConfirmPublishStrategyFactory confirmPublishStrategyFactory;
    private final UserClient userClient;


    public OrderServiceImpl(OrderRepository orderRepository,
                            OrderItemRepository orderItemRepository,
                            CancelOrderStrategyFactory strategyFactory,
                            OrderConfirmPublishStrategyFactory confirmPublishStrategyFactory,
                            UserClient userClient) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.strategyFactory = strategyFactory;
        this.confirmPublishStrategyFactory = confirmPublishStrategyFactory;
        this.userClient = userClient;
    }

    @Override
    @Transactional
    public Long addItem(AddToCartRequest req) {
        OrdersCart draft = orderRepository
                .findFirstByCustomerIdAndStatus(req.getCustomerId(), OrderStatus.DRAFT)
                .orElseGet(() -> {
                    UserStrategyPreferenceResponse strategyPreference = userClient.getStrategiesByCustomer(req.getCustomerId());
                    OrdersCart o = new OrdersCart();
                    o.setCustomerId(req.getCustomerId());
                    o.setStatus(OrderStatus.DRAFT);
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
                    return oi;
                });

        item.setQty(item.getQty() + req.getQty());
        orderItemRepository.save(item);

        return draft.getOrderId();
    }

    @Override
    @Transactional
    public Boolean cancelCart(Long orderId, CancelReason reason) {
        OrdersCart order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Sipariş bulunamadı."));

        CancelOrderStrategy strategy = strategyFactory.getStrategy(reason);
        strategy.cancel(order);
        return Boolean.TRUE;
    }

    @Override
    @Transactional
    public void confirmCart(Long orderId) {

        OrdersCart order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Sipariş bulunamadı. id=" + orderId));

        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new BadRequestException("Sepet boş.");
        }

        OrderStatusRules.requireCanConfirm(order.getStatus());
        order.setStatus(OrderStatus.PENDING);

        orderRepository.saveAndFlush(order);

        List<OrderItemDto> items = order.getItems().stream()
                .map(i -> new OrderItemDto(i.getProductId(), i.getQty()))
                .toList();

        confirmPublishStrategyFactory
                .getRequired(order.getOrderType())
                .publish(order, items);
    }
}
