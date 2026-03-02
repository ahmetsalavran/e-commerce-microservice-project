package com.ars.user.controller;

import com.ars.contract.user.UserStrategyPreferenceResponse;
import com.ars.user.service.UserProfileService;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/users")
public class UserProfileController {

    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping("/by-customer/{customerId}/strategies")
    public UserStrategyPreferenceResponse getStrategiesByCustomer(@PathVariable @Positive Long customerId) {
        return userProfileService.getStrategiesByCustomerId(customerId);
    }
}
