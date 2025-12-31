package org.com.smartpayments.subscription.core.domain.usecase.userSubscription.cancelSubscription.exception;

import org.com.smartpayments.subscription.core.common.exception.base.BadRequestException;

public class InvalidCancellationException extends BadRequestException {
    public InvalidCancellationException(String message) {
        super(message);
    }
}
