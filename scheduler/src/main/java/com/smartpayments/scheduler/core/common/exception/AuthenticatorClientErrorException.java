package com.smartpayments.scheduler.core.common.exception;

public class AuthenticatorClientErrorException extends RuntimeException {
    public AuthenticatorClientErrorException(String message) {
        super(message);
    }
}
