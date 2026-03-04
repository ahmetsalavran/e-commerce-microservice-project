package com.ars.user.service;

import com.ars.contract.user.UserStrategyPreferenceResponse;

public interface UserProfileService {
    UserStrategyPreferenceResponse getStrategiesByCustomerId(Long customerId);
}
