package com.smartpayments.scheduler.core.domain.usecase.paymentScheduledNotification.schedulePaymentNotification.exception;

public class ScheduledPaymentNotificationAlreadyExists extends RuntimeException {
    public ScheduledPaymentNotificationAlreadyExists(String message) {
        super(message);
    }
}
