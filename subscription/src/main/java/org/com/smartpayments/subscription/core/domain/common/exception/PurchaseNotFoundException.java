package org.com.smartpayments.subscription.core.domain.common.exception;

public class PurchaseNotFoundException extends RuntimeException {
    public PurchaseNotFoundException(String message) {
        super(message);
    }
}
