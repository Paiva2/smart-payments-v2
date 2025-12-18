package org.com.smartpayments.subscription.core.common.exception;

import org.com.smartpayments.subscription.core.common.exception.base.BadRequestException;

public class GenericException extends BadRequestException {
    public GenericException(String message) {
        super(message);
    }
}
