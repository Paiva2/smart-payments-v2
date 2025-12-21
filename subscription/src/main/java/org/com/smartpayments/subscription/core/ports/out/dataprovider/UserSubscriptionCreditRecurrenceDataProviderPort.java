package org.com.smartpayments.subscription.core.ports.out.dataprovider;

import org.com.smartpayments.subscription.core.domain.model.UserSubscriptionCreditRecurrence;

import java.util.List;

public interface UserSubscriptionCreditRecurrenceDataProviderPort {
    List<UserSubscriptionCreditRecurrence> persistAll(List<UserSubscriptionCreditRecurrence> userSubscriptionCreditRecurrences);

    List<UserSubscriptionCreditRecurrence> findAllByUserSubscription(Long userSubscriptionId);

    void deleteAllByUserSubscription(Long userSubscriptionId);
}
