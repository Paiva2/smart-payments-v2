package org.com.smartpayments.subscription.core.common.exception;

import org.com.smartpayments.subscription.core.common.base.NotFoundException;

public class PurchaseChargeNotFoundException extends NotFoundException {
    public PurchaseChargeNotFoundException() {
        super("Purchase charge not found!");
    }
}
