package org.com.smartpayments.authenticator.core.common.exception;

public class GenericCpfCnpjInvalidException extends RuntimeException {
    public GenericCpfCnpjInvalidException(String message) {
        super(String.format("Invalid document. %s", message));
    }
}
