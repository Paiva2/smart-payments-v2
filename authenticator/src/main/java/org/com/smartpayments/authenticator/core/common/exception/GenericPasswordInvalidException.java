package org.com.smartpayments.authenticator.core.common.exception;

public class GenericPasswordInvalidException extends RuntimeException {
    public GenericPasswordInvalidException(String message) {
        super(String.format("Invalid password. %s", message));
    }
}
