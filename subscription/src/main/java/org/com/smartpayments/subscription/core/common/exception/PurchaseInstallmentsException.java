package org.com.smartpayments.subscription.core.common.exception;

import org.com.smartpayments.subscription.core.common.exception.base.BadRequestException;

public class PurchaseInstallmentsException extends BadRequestException {
    public PurchaseInstallmentsException(int installments) {
        super("Invalid stallments. Maximum installments allowed: " + installments);
    }
}
