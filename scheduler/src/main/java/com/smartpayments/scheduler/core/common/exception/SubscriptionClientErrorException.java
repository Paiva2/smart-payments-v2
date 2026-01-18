package com.smartpayments.scheduler.core.common.exception;

import com.smartpayments.scheduler.core.common.exception.base.BadRequestException;

public class SubscriptionClientErrorException extends BadRequestException {
    public SubscriptionClientErrorException(String message) {
        super(message);
    }
}
