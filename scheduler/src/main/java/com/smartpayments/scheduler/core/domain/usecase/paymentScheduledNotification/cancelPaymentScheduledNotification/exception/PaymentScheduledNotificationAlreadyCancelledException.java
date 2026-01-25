package com.smartpayments.scheduler.core.domain.usecase.paymentScheduledNotification.cancelPaymentScheduledNotification.exception;

import com.smartpayments.scheduler.core.common.exception.base.BadRequestException;

public class PaymentScheduledNotificationAlreadyCancelledException extends BadRequestException {
    public PaymentScheduledNotificationAlreadyCancelledException() {
        super("This payment notification is already cancelled!");
    }
}
