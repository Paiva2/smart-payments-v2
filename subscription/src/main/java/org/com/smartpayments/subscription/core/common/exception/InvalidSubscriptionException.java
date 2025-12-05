package org.com.smartpayments.subscription.core.common.exception;

import org.com.smartpayments.subscription.core.common.base.BadRequestException;

public class InvalidSubscriptionException extends BadRequestException {
    public InvalidSubscriptionException(String message) {
        super(String.format("Invalid subscription. %s", message));
    }
}
