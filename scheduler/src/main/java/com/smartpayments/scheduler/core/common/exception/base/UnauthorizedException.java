package com.smartpayments.scheduler.core.common.exception.base;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
