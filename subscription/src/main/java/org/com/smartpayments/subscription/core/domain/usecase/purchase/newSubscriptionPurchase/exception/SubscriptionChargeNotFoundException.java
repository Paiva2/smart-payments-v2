package org.com.smartpayments.subscription.core.domain.usecase.purchase.newSubscriptionPurchase.exception;

import org.com.smartpayments.subscription.core.common.base.NotFoundException;

public class SubscriptionChargeNotFoundException extends NotFoundException {
    public SubscriptionChargeNotFoundException() {
        super("Subscription charge not found!");
    }
}
