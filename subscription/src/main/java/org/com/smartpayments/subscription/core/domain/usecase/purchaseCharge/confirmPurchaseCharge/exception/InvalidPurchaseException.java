package org.com.smartpayments.subscription.core.domain.usecase.purchaseCharge.confirmPurchaseCharge.exception;

import org.com.smartpayments.subscription.core.common.base.BadRequestException;

public class InvalidPurchaseException extends BadRequestException {
    public InvalidPurchaseException(String message) {
        super(message);
    }
}
