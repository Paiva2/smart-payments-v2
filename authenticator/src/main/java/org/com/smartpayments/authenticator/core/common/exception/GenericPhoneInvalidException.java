package org.com.smartpayments.authenticator.core.common.exception;

import org.com.smartpayments.authenticator.core.common.exception.base.BadRequestException;

public class GenericPhoneInvalidException extends BadRequestException {
    public GenericPhoneInvalidException(String message) {
        super(String.format("Invalid phone. %s", message));
    }
}
