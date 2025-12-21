package org.com.smartpayments.subscription.infra.adapters.dataProvider;

import lombok.AllArgsConstructor;
import org.com.smartpayments.subscription.core.domain.model.UserSubscriptionCreditHistory;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.UserSubscriptionCreditHistoryDataProviderPort;
import org.com.smartpayments.subscription.infra.persistence.repository.UserSubscriptionCreditHistoryRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class UserSubscriptionCreditHistoryDataProviderAdapter implements UserSubscriptionCreditHistoryDataProviderPort {
    private final UserSubscriptionCreditHistoryRepository repository;

    @Override
    public List<UserSubscriptionCreditHistory> persistAll(List<UserSubscriptionCreditHistory> userSubscriptionCreditHistories) {
        return repository.saveAll(userSubscriptionCreditHistories);
    }

    @Override
    public void revokeSampleCredits(Long userSubscriptionId) {
        repository.revokeSampleCreditsByUserSubscriptionId(userSubscriptionId);
    }

    @Override
    public void revokeNonExpiredCredits(Long userSubscriptionId) {
        repository.revokeNonExpiredCreditsByUserSubscriptionId(userSubscriptionId);
    }
}
