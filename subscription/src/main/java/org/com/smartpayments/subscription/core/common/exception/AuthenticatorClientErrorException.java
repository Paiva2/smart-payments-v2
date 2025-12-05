package org.com.smartpayments.subscription.core.common.exception;

import org.com.smartpayments.subscription.core.common.base.BadRequestException;

public class AuthenticatorClientErrorException extends BadRequestException {
    public AuthenticatorClientErrorException(String message) {
        super(message);
    }
}
