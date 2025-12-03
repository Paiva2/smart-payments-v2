package org.com.smartpayments.subscription.core.domain.common.base;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
