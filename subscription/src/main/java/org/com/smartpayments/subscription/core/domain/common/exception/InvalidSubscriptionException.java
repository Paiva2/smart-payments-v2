package org.com.smartpayments.subscription.core.domain.common.exception;

import org.com.smartpayments.subscription.core.domain.common.base.BadRequestException;

public class InvalidSubscriptionException extends BadRequestException {
    public InvalidSubscriptionException(String message) {
        super(String.format("Invalid subscription. %s", message));
    }
}
