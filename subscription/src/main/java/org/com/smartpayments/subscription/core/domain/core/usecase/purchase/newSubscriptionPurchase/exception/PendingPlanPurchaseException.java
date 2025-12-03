package org.com.smartpayments.subscription.core.domain.core.usecase.purchase.newSubscriptionPurchase.exception;

public class PendingPlanPurchaseException extends RuntimeException {
    public PendingPlanPurchaseException() {
        super("User has an pending plan purchase. Can't buy other plan while having a pending plan purchase!");
    }
}
