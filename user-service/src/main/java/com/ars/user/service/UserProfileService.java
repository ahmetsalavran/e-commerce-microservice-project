package com.ars.user.service;

import com.ars.contract.strategy.InventoryStrategy;
import com.ars.contract.strategy.PaymentStrategy;
import com.ars.contract.user.UserStrategyPreferenceResponse;
import com.ars.core.infrastructure.web.error.NotFoundException;
import com.ars.user.entity.UserProfile;
import com.ars.user.repository.UserProfileRepository;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;

    public UserProfileService(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    public UserStrategyPreferenceResponse getStrategiesByCustomerId(Long customerId) {
        UserProfile profile = userProfileRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new NotFoundException("User profile not found by customerId=" + customerId));

        return new UserStrategyPreferenceResponse(
                profile.getCustomerId(),
                Objects.requireNonNullElse(profile.getInventoryStrategy(), InventoryStrategy.ALL_OR_NOTHING),
                Objects.requireNonNullElse(profile.getPaymentStrategy(), PaymentStrategy.THIRD_PARTY_THEN_LOCAL)
        );
    }
}
