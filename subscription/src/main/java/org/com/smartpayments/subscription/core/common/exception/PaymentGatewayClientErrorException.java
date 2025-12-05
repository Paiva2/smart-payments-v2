package org.com.smartpayments.subscription.core.common.exception;

import org.com.smartpayments.subscription.core.common.base.BadRequestException;

public class PaymentGatewayClientErrorException extends BadRequestException {
    public PaymentGatewayClientErrorException(String message) {
        super(message);
    }
}
