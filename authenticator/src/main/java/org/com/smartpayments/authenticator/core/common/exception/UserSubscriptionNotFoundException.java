package org.com.smartpayments.authenticator.core.common.exception;

import org.com.smartpayments.authenticator.core.common.exception.base.NotFoundException;

public class UserSubscriptionNotFoundException extends NotFoundException {
    public UserSubscriptionNotFoundException() {
        super("User subscription not found!");
    }
}
