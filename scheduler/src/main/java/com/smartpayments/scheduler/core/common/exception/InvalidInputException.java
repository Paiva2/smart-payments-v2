package com.smartpayments.scheduler.core.common.exception;

import com.smartpayments.scheduler.core.common.exception.base.BadRequestException;

public class InvalidInputException extends BadRequestException {
    public InvalidInputException(String message) {
        super(message);
    }
}
