package org.com.smartpayments.subscription.core.ports.out.dataprovider;

import org.com.smartpayments.subscription.core.domain.model.UserSubscription;

public interface UserSubscriptionDataProviderPort {
    UserSubscription persist(UserSubscription userSubscription);
}
