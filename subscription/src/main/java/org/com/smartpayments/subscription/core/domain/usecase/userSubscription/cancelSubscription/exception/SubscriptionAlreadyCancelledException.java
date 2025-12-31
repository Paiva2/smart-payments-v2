package org.com.smartpayments.subscription.core.domain.usecase.userSubscription.cancelSubscription.exception;

import org.com.smartpayments.subscription.core.common.exception.base.BadRequestException;

public class SubscriptionAlreadyCancelledException extends BadRequestException {
    public SubscriptionAlreadyCancelledException() {
        super("User subscription is already cancelled!");
    }
}
