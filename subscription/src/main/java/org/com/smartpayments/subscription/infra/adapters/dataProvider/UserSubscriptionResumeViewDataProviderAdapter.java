package org.com.smartpayments.subscription.infra.adapters.dataProvider;

import lombok.AllArgsConstructor;
import org.com.smartpayments.subscription.core.domain.model.views.UserSubscriptionResumeView;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.UserSubscriptionResumeViewDataProviderPort;
import org.com.smartpayments.subscription.infra.persistence.repository.UserSubscriptionViewResumeRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@AllArgsConstructor
public class UserSubscriptionResumeViewDataProviderAdapter implements UserSubscriptionResumeViewDataProviderPort {
    private final UserSubscriptionViewResumeRepository repository;

    @Override
    public Optional<UserSubscriptionResumeView> findByUser(Long userId) {
        return repository.findByUserId(userId);
    }
}
