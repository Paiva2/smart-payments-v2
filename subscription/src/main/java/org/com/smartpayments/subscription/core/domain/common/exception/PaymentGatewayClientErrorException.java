package org.com.smartpayments.subscription.core.domain.common.exception;

import org.com.smartpayments.subscription.core.domain.common.base.BadRequestException;

public class PaymentGatewayClientErrorException extends BadRequestException {
    public PaymentGatewayClientErrorException(String message) {
        super(message);
    }
}
