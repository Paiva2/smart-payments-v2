package com.smartpayments.scheduler.core.domain.usecase.paymentScheduledNotification.schedulePaymentNotification.exception;

public class InvalidPaymentScheduledDateException extends RuntimeException {
    public InvalidPaymentScheduledDateException(String message) {
        super(message);
    }
}
