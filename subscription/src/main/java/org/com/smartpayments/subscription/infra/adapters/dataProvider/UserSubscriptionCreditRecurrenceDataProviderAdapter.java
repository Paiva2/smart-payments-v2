package org.com.smartpayments.subscription.infra.adapters.dataProvider;

import lombok.AllArgsConstructor;
import org.com.smartpayments.subscription.core.domain.model.UserSubscriptionCreditRecurrence;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.UserSubscriptionCreditRecurrenceDataProviderPort;
import org.com.smartpayments.subscription.infra.persistence.repository.UserSubscriptionCreditRecurrenceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserSubscriptionCreditRecurrenceDataProviderAdapter implements UserSubscriptionCreditRecurrenceDataProviderPort {
    private final UserSubscriptionCreditRecurrenceRepository repository;

    @Override
    public List<UserSubscriptionCreditRecurrence> persistAll(List<UserSubscriptionCreditRecurrence> userSubscriptionCreditRecurrences) {
        return repository.saveAll(userSubscriptionCreditRecurrences);
    }
}
