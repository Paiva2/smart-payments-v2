package org.com.smartpayments.authenticator.core.common.exception.base;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
