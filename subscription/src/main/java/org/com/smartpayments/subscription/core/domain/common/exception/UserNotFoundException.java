package org.com.smartpayments.subscription.core.domain.common.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException() {
        super("User not found!");
    }
}
