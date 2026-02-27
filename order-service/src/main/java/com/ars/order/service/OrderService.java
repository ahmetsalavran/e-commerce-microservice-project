package com.ars.order.service;

import com.ars.order.models.enums.CancelReason;
import com.ars.order.models.request.AddToCartRequest;

public interface OrderService {
    Long addItem(AddToCartRequest req);
    Boolean cancelCart(Long orderId, CancelReason reason);
    void confirmCart(Long orderId);
}
