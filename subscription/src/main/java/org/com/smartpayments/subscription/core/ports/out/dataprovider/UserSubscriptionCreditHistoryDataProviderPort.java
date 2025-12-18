package org.com.smartpayments.subscription.core.ports.out.dataprovider;

import org.com.smartpayments.subscription.core.domain.model.UserSubscriptionCreditHistory;

import java.util.List;

public interface UserSubscriptionCreditHistoryDataProviderPort {
    List<UserSubscriptionCreditHistory> persistAll(List<UserSubscriptionCreditHistory> userSubscriptionCreditHistories);

    void revokeSampleCredits(Long userSubscriptionId);
}
