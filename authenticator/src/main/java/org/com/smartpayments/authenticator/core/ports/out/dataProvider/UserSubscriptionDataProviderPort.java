package org.com.smartpayments.authenticator.core.ports.out.dataProvider;

import org.com.smartpayments.authenticator.core.domain.model.UserSubscription;

import java.util.Optional;

public interface UserSubscriptionDataProviderPort {
    Optional<UserSubscription> findByUserId(Long userId);

    UserSubscription persist(UserSubscription userSubscription);
}
