package org.com.smartpayments.subscription.core.domain.core.usecase.purchase.newSubscriptionPurchase.exception;

public class SubscriptionChargeNotFoundException extends RuntimeException {
    public SubscriptionChargeNotFoundException() {
        super("Subscription charge not found!");
    }
}
