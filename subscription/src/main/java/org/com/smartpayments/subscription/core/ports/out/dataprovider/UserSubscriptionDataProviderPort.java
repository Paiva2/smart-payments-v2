package org.com.smartpayments.subscription.core.ports.out.dataprovider;

import org.com.smartpayments.subscription.core.domain.model.UserSubscription;

import java.util.Optional;

public interface UserSubscriptionDataProviderPort {
    UserSubscription persist(UserSubscription userSubscription);

    Optional<UserSubscription> findByUserWithPlan(Long userId);
}
