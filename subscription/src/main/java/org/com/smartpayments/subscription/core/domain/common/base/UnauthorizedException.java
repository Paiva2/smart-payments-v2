package org.com.smartpayments.subscription.core.domain.common.base;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
