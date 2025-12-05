package org.com.smartpayments.subscription.core.domain.usecase.purchase.newCreditsPurchase.validator;

import org.com.smartpayments.subscription.core.common.exception.PurchaseItemGenericException;
import org.com.smartpayments.subscription.core.ports.in.dto.NewCreditsPurchaseInput;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static java.util.Objects.isNull;

public class NewCreditsPurchaseValidator {
    public static void validate(NewCreditsPurchaseInput input) {
        if (input.getPurchaseItems().isEmpty()) {
            throw new PurchaseItemGenericException("Purchase items cannot be empty!");
        }

        boolean hasPurchaseItemWithoutCreditId = input.getPurchaseItems()
            .stream().anyMatch(pi -> isNull(pi.getCreditId()));

        if (hasPurchaseItemWithoutCreditId) {
            throw new PurchaseItemGenericException("Credit id must be provided!");
        }

        boolean hasPurchaseItemWithInvalidQuantity = input.getPurchaseItems()
            .stream().anyMatch(pi -> pi.getQuantity() < 1);

        if (hasPurchaseItemWithInvalidQuantity) {
            throw new PurchaseItemGenericException("Quantity can't be less than 1!");
        }

        List<Long> creditsIdsPurchased = input.getPurchaseItems()
            .stream().map(NewCreditsPurchaseInput.PurchaseItemInput::getCreditId)
            .filter(Objects::nonNull).toList();

        Set<Long> creditsIdPurchasedNonRepeatable = new HashSet<>(creditsIdsPurchased);

        if (creditsIdsPurchased.size() != creditsIdPurchasedNonRepeatable.size()) {
            throw new PurchaseItemGenericException("Can't repeat a credit id on purchase items, instead increase quantity or remove duplicated!");
        }
    }
}
