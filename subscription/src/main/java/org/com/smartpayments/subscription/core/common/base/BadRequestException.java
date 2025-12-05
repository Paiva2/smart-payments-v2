package org.com.smartpayments.subscription.core.common.base;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
