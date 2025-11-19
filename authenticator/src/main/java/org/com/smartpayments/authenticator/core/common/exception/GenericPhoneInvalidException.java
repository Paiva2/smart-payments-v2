package org.com.smartpayments.authenticator.core.common.exception;

public class GenericPhoneInvalidException extends RuntimeException {
    public GenericPhoneInvalidException(String message) {
        super(String.format("Invalid phone. %s", message));
    }
}
