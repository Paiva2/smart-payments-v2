package org.com.smartpayments.subscription.core.domain.common.base;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
