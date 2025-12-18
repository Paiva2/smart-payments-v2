package org.com.smartpayments.subscription.core.common.exception;

import org.com.smartpayments.subscription.core.common.base.NotFoundException;

public class UserSubscriptionResumeViewNotFoundException extends NotFoundException {
    public UserSubscriptionResumeViewNotFoundException() {
        super("User subscription resume not found!");
    }
}
