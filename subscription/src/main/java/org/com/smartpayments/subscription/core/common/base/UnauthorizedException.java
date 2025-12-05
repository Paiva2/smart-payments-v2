package org.com.smartpayments.subscription.core.common.base;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
