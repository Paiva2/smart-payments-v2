package com.smartpayments.scheduler.core.common.exception;

public class InvalidAuthTokenException extends RuntimeException {
    public InvalidAuthTokenException() {
        super("Invalid Authorization token!");
    }
}