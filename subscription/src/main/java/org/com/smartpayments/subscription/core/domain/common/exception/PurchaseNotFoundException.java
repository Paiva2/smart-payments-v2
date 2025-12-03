package org.com.smartpayments.subscription.core.domain.common.exception;

import org.com.smartpayments.subscription.core.domain.common.base.NotFoundException;

public class PurchaseNotFoundException extends NotFoundException {
    public PurchaseNotFoundException(String message) {
        super(message);
    }
}
