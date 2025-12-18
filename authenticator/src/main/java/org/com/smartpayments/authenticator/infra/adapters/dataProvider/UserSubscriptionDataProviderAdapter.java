package org.com.smartpayments.authenticator.infra.adapters.dataProvider;

import lombok.AllArgsConstructor;
import org.com.smartpayments.authenticator.core.domain.model.UserSubscription;
import org.com.smartpayments.authenticator.core.ports.out.dataProvider.UserSubscriptionDataProviderPort;
import org.com.smartpayments.authenticator.infra.persistence.repository.UserSubscriptionRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@AllArgsConstructor
public class UserSubscriptionDataProviderAdapter implements UserSubscriptionDataProviderPort {
    private final UserSubscriptionRepository repository;

    @Override
    public Optional<UserSubscription> findByUserId(Long userId) {
        return repository.findByUserId(userId);
    }

    @Override
    public UserSubscription persist(UserSubscription userSubscription) {
        return repository.save(userSubscription);
    }
}
