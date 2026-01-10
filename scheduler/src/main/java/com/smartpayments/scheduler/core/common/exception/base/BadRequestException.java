package com.smartpayments.scheduler.core.common.exception.base;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
