package org.com.smartpayments.subscription.core.domain.common.exception;

import org.com.smartpayments.subscription.core.domain.common.base.BadRequestException;

public class PurchaseInstallmentsException extends BadRequestException {
    public PurchaseInstallmentsException(int installments) {
        super("Invalid stallments. Maximum installments allowed: " + installments);
    }
}
