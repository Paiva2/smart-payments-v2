package org.com.smartpayments.authenticator.core.common.exception;

import org.com.smartpayments.authenticator.core.common.exception.base.BadRequestException;

public class GenericPasswordInvalidException extends BadRequestException {
    public GenericPasswordInvalidException(String message) {
        super(String.format("Invalid password. %s", message));
    }
}
