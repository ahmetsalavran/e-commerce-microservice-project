package com.ars.order.client;

import com.ars.contract.user.UserStrategyPreferenceResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", url = "${clients.user.url}")
public interface UserClient {

    @GetMapping("/users/by-customer/{customerId}/strategies")
    UserStrategyPreferenceResponse getStrategiesByCustomer(@PathVariable("customerId") Long customerId);
}
