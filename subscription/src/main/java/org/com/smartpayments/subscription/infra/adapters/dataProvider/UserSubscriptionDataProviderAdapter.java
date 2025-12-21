package org.com.smartpayments.subscription.infra.adapters.dataProvider;

import lombok.AllArgsConstructor;
import org.com.smartpayments.subscription.core.domain.model.UserSubscription;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.UserSubscriptionDataProviderPort;
import org.com.smartpayments.subscription.infra.persistence.repository.UserSubscriptionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserSubscriptionDataProviderAdapter implements UserSubscriptionDataProviderPort {
    private final UserSubscriptionRepository repository;

    @Override
    public UserSubscription persist(UserSubscription userSubscription) {
        return repository.save(userSubscription);
    }

    @Override
    public Optional<UserSubscription> findByUserWithPlan(Long userId) {
        return repository.findByUserIdWithPlan(userId);
    }

    @Override
    public Optional<UserSubscription> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Optional<UserSubscription> findByExternalSubscriptionId(String externalId) {
        return repository.findByExternalSubscriptionId(externalId);
    }

    @Override
    public List<UserSubscription> findAllMonthlyToRenew() {
        return repository.findAllMonthlyToRenewNextPaymentDate();
    }

    @Override
    public List<UserSubscription> findAllMonthlyToRevoke() {
        return repository.findAllMonthlyToRevoke();
    }
}
