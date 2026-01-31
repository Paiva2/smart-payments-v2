package com.smartpayments.scheduler.core.common.exception;

public class PaymentScheduledNotificationCancelledException extends RuntimeException {
    public PaymentScheduledNotificationCancelledException(String message) {
        super(message);
    }
}
