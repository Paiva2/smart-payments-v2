package com.smartpayments.scheduler.core.common.exception;

import com.smartpayments.scheduler.core.common.exception.base.BadRequestException;

public class AuthenticatorClientErrorException extends BadRequestException {
    public AuthenticatorClientErrorException(String message) {
        super(message);
    }
}
