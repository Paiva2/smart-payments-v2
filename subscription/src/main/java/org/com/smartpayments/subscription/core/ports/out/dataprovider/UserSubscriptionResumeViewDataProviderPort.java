package org.com.smartpayments.subscription.core.ports.out.dataprovider;

import org.com.smartpayments.subscription.core.domain.model.views.UserSubscriptionResumeView;

import java.util.Optional;

public interface UserSubscriptionResumeViewDataProviderPort {
    Optional<UserSubscriptionResumeView> findByUser(Long userId);
}
