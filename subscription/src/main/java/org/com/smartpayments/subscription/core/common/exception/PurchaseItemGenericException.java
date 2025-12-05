package org.com.smartpayments.subscription.core.common.exception;

import org.com.smartpayments.subscription.core.common.base.BadRequestException;

public class PurchaseItemGenericException extends BadRequestException {
    public PurchaseItemGenericException(String message) {
        super(String.format("Invalid purchase item. %s", message));
    }
}
