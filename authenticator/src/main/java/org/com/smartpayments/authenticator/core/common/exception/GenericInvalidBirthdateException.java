package org.com.smartpayments.authenticator.core.common.exception;

public class GenericInvalidBirthdateException extends RuntimeException {
    public GenericInvalidBirthdateException(String message) {
        super(String.format("Invalid birthdate. %s", message));
    }
}
