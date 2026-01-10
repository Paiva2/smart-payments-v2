package com.smartpayments.scheduler.core.domain.usecase.paymentScheduledNotification.schedulePaymentNotification.exception;

import com.smartpayments.scheduler.core.common.exception.base.ConflictException;

public class ScheduledPaymentNotificationAlreadyExists extends ConflictException {
    public ScheduledPaymentNotificationAlreadyExists(String message) {
        super(message);
    }
}
