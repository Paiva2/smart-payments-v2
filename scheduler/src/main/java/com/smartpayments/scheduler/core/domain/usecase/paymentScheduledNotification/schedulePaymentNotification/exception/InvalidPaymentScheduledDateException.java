package com.smartpayments.scheduler.core.domain.usecase.paymentScheduledNotification.schedulePaymentNotification.exception;

import com.smartpayments.scheduler.core.common.exception.base.BadRequestException;

public class InvalidPaymentScheduledDateException extends BadRequestException {
    public InvalidPaymentScheduledDateException(String message) {
        super(message);
    }
}
