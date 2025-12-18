package org.com.smartpayments.subscription.core.domain.usecase.purchaseCharge.confirmPurchaseCharge.exception;

import org.com.smartpayments.subscription.core.common.base.BadRequestException;

public class UserAlreadyHasActivePlanException extends BadRequestException {
    public UserAlreadyHasActivePlanException(String message) {
        super(message);
    }
}
