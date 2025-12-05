package org.com.smartpayments.subscription.core.common.base;

public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
