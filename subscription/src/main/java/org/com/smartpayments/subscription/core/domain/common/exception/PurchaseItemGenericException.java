package org.com.smartpayments.subscription.core.domain.common.exception;

public class PurchaseItemGenericException extends RuntimeException {
    public PurchaseItemGenericException(String message) {
        super(String.format("Invalid purchase item. %s", message));
    }
}
