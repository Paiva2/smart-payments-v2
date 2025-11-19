package org.com.smartpayments.authenticator.core.common.exception;

public class RoleNotFoundException extends RuntimeException {
    public RoleNotFoundException(String message) {
        super(String.format("Role '%s' not found.", message));
    }
}
