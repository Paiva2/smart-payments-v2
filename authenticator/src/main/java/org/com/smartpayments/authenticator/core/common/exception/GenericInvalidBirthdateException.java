package org.com.smartpayments.authenticator.core.common.exception;

import org.com.smartpayments.authenticator.core.common.exception.base.BadRequestException;

public class GenericInvalidBirthdateException extends BadRequestException {
    public GenericInvalidBirthdateException(String message) {
        super(String.format("Invalid birthdate. %s", message));
    }
}
