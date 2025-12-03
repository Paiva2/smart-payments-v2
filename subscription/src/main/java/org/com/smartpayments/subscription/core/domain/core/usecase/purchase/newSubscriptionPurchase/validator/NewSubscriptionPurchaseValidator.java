package org.com.smartpayments.subscription.core.domain.core.usecase.purchase.newSubscriptionPurchase.validator;

import org.com.smartpayments.subscription.core.domain.common.exception.PurchaseInstallmentsException;
import org.com.smartpayments.subscription.core.domain.common.exception.PurchaseItemGenericException;
import org.com.smartpayments.subscription.core.domain.core.ports.in.dto.NewSubscriptionPurchaseInput;

import static java.util.Objects.isNull;

public class NewSubscriptionPurchaseValidator {
    private final static Integer MAX_INSTALLMENTS_ALLOWED = 0;

    public static void validate(NewSubscriptionPurchaseInput input) {
        boolean hasPurchaseItemWithoutPurchaseId = input.getPurchaseItems()
            .stream().anyMatch(pi -> isNull(pi.getPlanId()));

        if (hasPurchaseItemWithoutPurchaseId) {
            throw new PurchaseItemGenericException("Plan id must be provided!");
        }
        
        if (input.getPurchaseItems().size() > 1) {
            throw new PurchaseItemGenericException("Can't insert a plan twice on purchase items!");
        }

        if (input.getInstallments() > MAX_INSTALLMENTS_ALLOWED) {
            throw new PurchaseInstallmentsException(MAX_INSTALLMENTS_ALLOWED);
        }
    }
}
