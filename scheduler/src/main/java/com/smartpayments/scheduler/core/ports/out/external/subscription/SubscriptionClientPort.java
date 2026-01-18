package com.smartpayments.scheduler.core.ports.out.external.subscription;

import com.smartpayments.scheduler.core.ports.in.external.messaging.UserSubscriptionCreditInput;
import com.smartpayments.scheduler.core.ports.out.external.dto.ConsumeUserSubscriptionCreditsOutput;
import com.smartpayments.scheduler.core.ports.out.external.dto.UserSubscriptionOutput;

public interface SubscriptionClientPort {
    UserSubscriptionOutput getUserSubscription(Long userId);

    ConsumeUserSubscriptionCreditsOutput consumeUserSubscriptionCredits(String idempotencyHash, UserSubscriptionCreditInput input);
}
