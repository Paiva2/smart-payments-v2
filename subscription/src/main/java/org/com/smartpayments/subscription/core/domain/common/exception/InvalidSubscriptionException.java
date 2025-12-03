package org.com.smartpayments.subscription.core.domain.common.exception;

public class InvalidSubscriptionException extends RuntimeException {
    public InvalidSubscriptionException(String message) {
        super(String.format("Invalid subscription. %s", message));
    }
}
