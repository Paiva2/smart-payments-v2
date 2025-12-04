package org.com.smartpayments.subscription.core.domain.common.exception;


import org.com.smartpayments.subscription.core.domain.common.base.UnauthorizedException;

public class InvalidAuthTokenException extends UnauthorizedException {
    public InvalidAuthTokenException() {
        super("Invalid Authorization token!");
    }
}
