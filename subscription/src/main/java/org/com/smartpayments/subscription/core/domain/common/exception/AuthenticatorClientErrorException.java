package org.com.smartpayments.subscription.core.domain.common.exception;

import org.com.smartpayments.subscription.core.domain.common.base.BadRequestException;

public class AuthenticatorClientErrorException extends BadRequestException {
    public AuthenticatorClientErrorException(String message) {
        super(message);
    }
}
