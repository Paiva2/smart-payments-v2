package org.com.smartpayments.subscription.core.common.exception;

import org.com.smartpayments.subscription.core.common.exception.base.NotFoundException;

public class UserSubscriptionNotFoundException extends NotFoundException {
    public UserSubscriptionNotFoundException() {
        super("User subscription not found!");
    }
}
