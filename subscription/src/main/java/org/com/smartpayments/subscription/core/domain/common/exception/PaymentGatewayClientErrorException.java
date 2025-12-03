package org.com.smartpayments.subscription.core.domain.common.exception;

public class PaymentGatewayClientErrorException extends RuntimeException {
    public PaymentGatewayClientErrorException(String message) {
        super(message);
    }
}
