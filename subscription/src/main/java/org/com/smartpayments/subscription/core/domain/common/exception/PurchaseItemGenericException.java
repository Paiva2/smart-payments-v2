package org.com.smartpayments.subscription.core.domain.common.exception;

import org.com.smartpayments.subscription.core.domain.common.base.BadRequestException;

public class PurchaseItemGenericException extends BadRequestException {
    public PurchaseItemGenericException(String message) {
        super(String.format("Invalid purchase item. %s", message));
    }
}
