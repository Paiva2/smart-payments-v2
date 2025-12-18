package org.com.smartpayments.subscription.core.domain.usecase.purchaseCharge.confirmPurchaseCharge.exception;

import org.com.smartpayments.subscription.core.common.base.BadRequestException;

public class MissingCreditPurchaseItemException extends BadRequestException {
    public MissingCreditPurchaseItemException(String message) {
        super(message);
    }
}
