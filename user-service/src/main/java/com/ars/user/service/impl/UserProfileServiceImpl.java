package com.ars.user.service.impl;

import com.ars.contract.strategy.InventoryStrategy;
import com.ars.contract.strategy.PaymentStrategy;
import com.ars.contract.user.UserStrategyPreferenceResponse;
import com.microservice.infrastructure.web.error.NotFoundException;
import com.ars.user.entity.UserProfile;
import com.ars.user.repository.UserProfileRepository;
import com.ars.user.service.UserProfileService;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileRepository userProfileRepository;

    public UserProfileServiceImpl(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    @Override
    public UserStrategyPreferenceResponse getStrategiesByCustomerId(Long customerId) {
        UserProfile profile = userProfileRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new NotFoundException("customerId için kullanıcı profili bulunamadı: " + customerId));

        return new UserStrategyPreferenceResponse(
                profile.getCustomerId(),
                Objects.requireNonNullElse(profile.getInventoryStrategy(), InventoryStrategy.ALL_OR_NOTHING),
                Objects.requireNonNullElse(profile.getPaymentStrategy(), PaymentStrategy.THIRD_PARTY_THEN_LOCAL)
        );
    }
}
