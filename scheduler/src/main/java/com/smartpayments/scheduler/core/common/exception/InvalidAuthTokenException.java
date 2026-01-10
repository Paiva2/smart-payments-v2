package com.smartpayments.scheduler.core.common.exception;

import com.smartpayments.scheduler.core.common.exception.base.UnauthorizedException;

public class InvalidAuthTokenException extends UnauthorizedException {
    public InvalidAuthTokenException() {
        super("Invalid Authorization token!");
    }
}