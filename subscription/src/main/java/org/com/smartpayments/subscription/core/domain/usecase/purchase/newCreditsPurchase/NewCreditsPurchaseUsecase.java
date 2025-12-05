package org.com.smartpayments.subscription.core.domain.usecase.purchase.newCreditsPurchase;

import lombok.AllArgsConstructor;
import org.com.smartpayments.subscription.core.common.exception.CreditNotFoundException;
import org.com.smartpayments.subscription.core.common.exception.UserNotFoundException;
import org.com.smartpayments.subscription.core.domain.enums.EPurchaseStatus;
import org.com.smartpayments.subscription.core.domain.enums.EPurchaseType;
import org.com.smartpayments.subscription.core.domain.model.Credit;
import org.com.smartpayments.subscription.core.domain.model.Purchase;
import org.com.smartpayments.subscription.core.domain.model.PurchaseItem;
import org.com.smartpayments.subscription.core.domain.model.User;
import org.com.smartpayments.subscription.core.domain.usecase.purchase.newCreditsPurchase.validator.NewCreditsPurchaseValidator;
import org.com.smartpayments.subscription.core.ports.in.UsecasePort;
import org.com.smartpayments.subscription.core.ports.in.dto.NewCreditsPurchaseInput;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.CreditDataProviderPort;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.PurchaseDataProviderPort;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.UserDataProviderPort;
import org.com.smartpayments.subscription.core.ports.out.dto.NewCreditsPurchaseOutput;
import org.com.smartpayments.subscription.core.ports.out.external.dto.CreateCreditChargeOutput;
import org.com.smartpayments.subscription.core.ports.out.external.dto.NewCreditChargeClientInput;
import org.com.smartpayments.subscription.core.ports.out.external.paymentGateway.PaymentGatewayCreditClientPort;
import org.com.smartpayments.subscription.core.ports.out.external.paymentGateway.PaymentGatewayUserClientPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static org.springframework.util.ObjectUtils.isEmpty;

@Service
@AllArgsConstructor
public class NewCreditsPurchaseUsecase implements UsecasePort<NewCreditsPurchaseInput, NewCreditsPurchaseOutput> {
    private final UserDataProviderPort userDataProviderPort;
    private final CreditDataProviderPort creditDataProviderPort;
    private final PurchaseDataProviderPort purchaseDataProviderPort;

    private final PaymentGatewayUserClientPort paymentGatewayUserClientPort;
    private final PaymentGatewayCreditClientPort paymentGatewayCreditClientPort;

    @Override
    @Transactional
    public NewCreditsPurchaseOutput execute(NewCreditsPurchaseInput input) {
        NewCreditsPurchaseValidator.validate(input);

        User user = findUser(input.getUserId());
        Purchase purchase = null;

        boolean willCreateUserExternal = isEmpty(user.getUserPaymentGatewayExternalId());

        user = ensureUserExistsPaymentGateway(user, willCreateUserExternal);

        try {
            purchase = fillPurchase(user, input);
            purchase = persistPurchase(purchase);

            CreateCreditChargeOutput createChargeOutput = createCreditsCharge(user, purchase);
            purchase.setExternalId(createChargeOutput.getId());
            String paymentUrl = !isEmpty(createChargeOutput.getPaymentLink()) ? createChargeOutput.getPaymentLink() : createChargeOutput.getInvoiceUrl();

            persistPurchase(purchase);

            return new NewCreditsPurchaseOutput(paymentUrl);
        } catch (Exception e) {
            if (willCreateUserExternal && !isEmpty(user.getUserPaymentGatewayExternalId())) {
                paymentGatewayUserClientPort.deleteUserClient(user);
            }

            if (nonNull(purchase) && !isEmpty(purchase.getExternalId())) {
                paymentGatewayCreditClientPort.deleteCharge(user, purchase.getExternalId());
            }

            throw e;
        }
    }

    private User findUser(Long id) {
        return userDataProviderPort.findActiveById(id).orElseThrow(UserNotFoundException::new);
    }

    private User ensureUserExistsPaymentGateway(User user, boolean willCreateUserExternal) {
        if (!willCreateUserExternal) return user;
        return createUserClientPaymentGateway(user);
    }

    private User createUserClientPaymentGateway(User user) {
        String externalUserId = paymentGatewayUserClientPort.newUserClient(user);
        user.setUserPaymentGatewayExternalId(externalUserId);
        return userDataProviderPort.persist(user);
    }

    private String mountPurchaseItemDescription(String itemPurchase, int quantity, BigDecimal value) {
        String formattedValue = NumberFormat.getCurrencyInstance(Locale.US).format(value.setScale(2, RoundingMode.HALF_EVEN));
        return String.format("Item: %s, Quantity: %d, Unit Value: %s", itemPurchase, quantity, formattedValue);
    }

    private void fillPurchaseItems(Purchase purchase, NewCreditsPurchaseInput input) {
        for (NewCreditsPurchaseInput.PurchaseItemInput purchasedCredit : input.getPurchaseItems()) {
            Credit credit = creditDataProviderPort.findById(purchasedCredit.getCreditId())
                .orElseThrow(() -> new CreditNotFoundException(purchasedCredit.getCreditId()));

            purchase.getItems().add(PurchaseItem.builder()
                .description(mountPurchaseItemDescription("CREDIT_" + credit.getType(), purchasedCredit.getQuantity(), credit.getValue()))
                .quantity(purchasedCredit.getQuantity())
                .value(credit.getValue())
                .credit(credit)
                .purchase(purchase)
                .build()
            );
        }
    }

    private BigDecimal sumPurchaseTotal(List<PurchaseItem> items) {
        return items.stream()
            .map(purchaseItem -> purchaseItem.getValue().multiply(new BigDecimal(purchaseItem.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Purchase fillPurchase(User user, NewCreditsPurchaseInput input) {
        Purchase purchase = Purchase.builder()
            .user(user)
            .paymentMethod(input.getPaymentMethod())
            .totalValue(BigDecimal.ZERO)
            .status(EPurchaseStatus.PENDING)
            .purchaseType(EPurchaseType.CREDIT)
            .installments(0)
            .items(new ArrayList<>())
            .build();

        fillPurchaseItems(purchase, input);

        purchase.setTotalValue(sumPurchaseTotal(purchase.getItems()));

        return purchase;
    }

    private Purchase persistPurchase(Purchase purchase) {
        return purchaseDataProviderPort.persist(purchase);
    }

    private String mountChargeDescription(List<PurchaseItem> purchaseItems) {
        return purchaseItems.stream().map(PurchaseItem::getDescription)
            .collect(Collectors.joining("\n"));
    }

    private CreateCreditChargeOutput createCreditsCharge(User user, Purchase purchase) {
        if (purchase.getItems().isEmpty()) {
            throw new CreditNotFoundException("Credits not found while creating external service credits charge!");
        }

        BigDecimal purchaseCreditsTotalValue = purchase.getItems()
            .stream().filter(puri -> nonNull(puri.getCredit()))
            .map(puri -> puri.getValue().multiply(new BigDecimal(puri.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        NewCreditChargeClientInput input = NewCreditChargeClientInput.builder()
            .customer(user.getUserPaymentGatewayExternalId())
            .billingType(purchase.getPaymentMethod().name())
            .value(purchaseCreditsTotalValue)
            .dueDate(new Date())
            .description(mountChargeDescription(purchase.getItems()))
            .externalReference(purchase.getId().toString())
            .build();

        return paymentGatewayCreditClientPort.createCreditCharge(user, input);
    }
}
