package com.smartpayments.scheduler.core.common.exception;

import com.smartpayments.scheduler.core.common.exception.base.NotFoundException;

public class PaymentScheduledNotificationNotFoundException extends NotFoundException {
    public PaymentScheduledNotificationNotFoundException() {
        super("Payment scheduled notification not found!");
    }
}
