package org.com.smartpayments.subscription.integration.utils;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.com.smartpayments.subscription.core.domain.enums.EPaymentMethod;
import org.com.smartpayments.subscription.core.domain.enums.EPurchaseStatus;
import org.com.smartpayments.subscription.core.domain.enums.EPurchaseType;
import org.com.smartpayments.subscription.core.domain.model.Credit;
import org.com.smartpayments.subscription.core.domain.model.Plan;
import org.com.smartpayments.subscription.core.domain.model.Purchase;
import org.com.smartpayments.subscription.core.domain.model.PurchaseItem;
import org.com.smartpayments.subscription.core.domain.model.User;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.PurchaseDataProviderPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;

import static java.util.Objects.isNull;

@Component
@AllArgsConstructor
public class PurchaseUtils {
    private final PurchaseDataProviderPort purchaseDataProviderPort;

    @SneakyThrows
    @Transactional
    public Purchase createPurchase(EPurchaseType type, User user, Plan plan, Credit credit) {
        Purchase purchase = Purchase.builder()
            .externalId("pay_id")
            .paymentMethod(EPaymentMethod.CREDIT_CARD)
            .totalValue(new BigDecimal("20.00"))
            .status(EPurchaseStatus.PENDING)
            .purchaseType(type)
            .installments(0)
            .user(user)
            .items(new ArrayList<>())
            .build();

        PurchaseItem purchaseItem = PurchaseItem.builder()
            .description("My Purchase Item")
            .quantity(1)
            .value(isNull(plan) ? credit.getValue() : plan.getValue())
            .purchase(purchase)
            .plan(plan)
            .credit(credit)
            .build();

        purchase.getItems().add(purchaseItem);

        return purchaseDataProviderPort.persist(purchase);
    }
}

