package com.ars.order.controller;


import com.ars.core.infrastructure.idempotency.ApiResponse;
import com.ars.order.models.enums.CancelReason;
import com.ars.order.models.request.AddToCartRequest;
import com.ars.order.models.request.CancelCartRequest;
import com.ars.order.service.OrderService;
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

    @GetMapping("/ok")
    public String ok() {
        return " order ok";
    }

    @PostMapping("/addToCart")
    public Long addToCart(@RequestBody AddToCartRequest request) {
        return orderService.addItem(request);
    }

    @PostMapping("/cancelCart")
    public Boolean cancelCart(@RequestBody CancelCartRequest request) {
        return orderService.cancelCart(request.getOrderId(), CancelReason.USER_REQUEST);
    }


    @PostMapping("/confirmCart")
    public ApiResponse<Void> confirmCart(@RequestBody CancelCartRequest request) {
        orderService.confirmCart(request.getOrderId());
        return ApiResponse.ok(null);
    }


}
