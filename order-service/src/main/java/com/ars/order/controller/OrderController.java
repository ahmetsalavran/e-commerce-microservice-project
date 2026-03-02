package com.ars.order.controller;

import com.ars.order.models.enums.CancelReason;
import com.ars.order.models.request.AddToCartRequest;
import com.ars.order.models.request.CancelCartRequest;
import com.ars.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;



@RestController
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/addToCart")
    public Long addToCart(@Valid @RequestBody AddToCartRequest request) {
        return orderService.addItem(request);
    }

    @PostMapping("/cancelCart")
    public Boolean cancelCart(@Valid @RequestBody CancelCartRequest request) {
        return orderService.cancelCart(request.getOrderId(), CancelReason.USER_REQUEST);
    }

    @PostMapping("/confirmCart")
    public void confirmCart(@Valid @RequestBody CancelCartRequest request) {
        orderService.confirmCart(request.getOrderId());
    }


}
