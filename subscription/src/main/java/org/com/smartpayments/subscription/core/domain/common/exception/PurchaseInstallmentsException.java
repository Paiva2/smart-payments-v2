package org.com.smartpayments.subscription.core.domain.common.exception;

public class PurchaseInstallmentsException extends RuntimeException {
    public PurchaseInstallmentsException(int installments) {
        super("Invalid stallments. Maximum installments allowed: " + installments);
    }
}
