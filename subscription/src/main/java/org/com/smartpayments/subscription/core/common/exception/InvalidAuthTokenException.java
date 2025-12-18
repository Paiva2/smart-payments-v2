package org.com.smartpayments.subscription.core.common.exception;


import org.com.smartpayments.subscription.core.common.exception.base.UnauthorizedException;

public class InvalidAuthTokenException extends UnauthorizedException {
    public InvalidAuthTokenException() {
        super("Invalid Authorization token!");
    }
}
