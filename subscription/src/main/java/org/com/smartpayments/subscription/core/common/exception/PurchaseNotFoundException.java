package org.com.smartpayments.subscription.core.common.exception;

import org.com.smartpayments.subscription.core.common.exception.base.NotFoundException;

public class PurchaseNotFoundException extends NotFoundException {
    public PurchaseNotFoundException(String message) {
        super(message);
    }
}
