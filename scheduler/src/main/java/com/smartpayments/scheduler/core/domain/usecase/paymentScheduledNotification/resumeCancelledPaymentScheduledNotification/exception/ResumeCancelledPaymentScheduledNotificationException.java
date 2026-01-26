package com.smartpayments.scheduler.core.domain.usecase.paymentScheduledNotification.resumeCancelledPaymentScheduledNotification.exception;

import com.smartpayments.scheduler.core.common.exception.base.BadRequestException;

public class ResumeCancelledPaymentScheduledNotificationException extends BadRequestException {
    public ResumeCancelledPaymentScheduledNotificationException(String message) {
        super(message);
    }
}
