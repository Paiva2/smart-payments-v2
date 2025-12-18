package org.com.smartpayments.subscription.core.ports.out.dataprovider;

import org.com.smartpayments.subscription.core.domain.model.UserSubscriptionCreditRecurrence;

import java.util.List;

public interface UserSubscriptionCreditRecurrenceDataProviderPort {
    List<UserSubscriptionCreditRecurrence> persistAll(List<UserSubscriptionCreditRecurrence> userSubscriptionCreditRecurrences);
}
