package org.com.smartpayments.subscription.infra.adapters.dataProvider;

import lombok.AllArgsConstructor;
import org.com.smartpayments.subscription.core.domain.model.UserSubscription;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.UserSubscriptionDataProviderPort;
import org.com.smartpayments.subscription.infra.persistence.repository.UserSubscriptionRepository;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserSubscriptionDataProviderAdapter implements UserSubscriptionDataProviderPort {
    private final UserSubscriptionRepository repository;

    @Override
    public UserSubscription persist(UserSubscription userSubscription) {
        return repository.save(userSubscription);
    }
}
