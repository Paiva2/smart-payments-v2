package org.com.smartpayments.authenticator.core.common.exception;

import org.com.smartpayments.authenticator.core.common.exception.base.UnauthorizedException;

public class InvalidAuthTokenException extends UnauthorizedException {
    public InvalidAuthTokenException() {
        super("Invalid Authorization token!");
    }
}
