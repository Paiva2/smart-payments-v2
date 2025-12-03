package org.com.smartpayments.subscription.core.domain.core.usecase.purchase.newSubscriptionPurchase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.smartpayments.subscription.core.domain.common.exception.InvalidSubscriptionException;
import org.com.smartpayments.subscription.core.domain.common.exception.PlanNotFoundException;
import org.com.smartpayments.subscription.core.domain.common.exception.UserNotFoundException;
import org.com.smartpayments.subscription.core.domain.core.ports.in.UsecasePort;
import org.com.smartpayments.subscription.core.domain.core.ports.in.dto.CreatePurchaseChargeInput;
import org.com.smartpayments.subscription.core.domain.core.ports.in.dto.NewSubscriptionPurchaseInput;
import org.com.smartpayments.subscription.core.domain.core.ports.out.dataprovider.PlanDataProviderPort;
import org.com.smartpayments.subscription.core.domain.core.ports.out.dataprovider.PurchaseDataProviderPort;
import org.com.smartpayments.subscription.core.domain.core.ports.out.dataprovider.UserDataProviderPort;
import org.com.smartpayments.subscription.core.domain.core.ports.out.dto.NewSubscriptionPurchaseOutput;
import org.com.smartpayments.subscription.core.domain.core.ports.out.external.PaymentGatewaySubscriptionClientPort;
import org.com.smartpayments.subscription.core.domain.core.ports.out.external.PaymentGatewayUserClientPort;
import org.com.smartpayments.subscription.core.domain.core.ports.out.external.dto.CreateSubscriptionOutput;
import org.com.smartpayments.subscription.core.domain.core.ports.out.external.dto.GetSubscriptionChargesOutput;
import org.com.smartpayments.subscription.core.domain.core.ports.out.external.dto.NewSubscriptionClientInput;
import org.com.smartpayments.subscription.core.domain.core.usecase.purchase.newSubscriptionPurchase.exception.SubscriptionChargeNotFoundException;
import org.com.smartpayments.subscription.core.domain.core.usecase.purchase.newSubscriptionPurchase.validator.NewSubscriptionPurchaseValidator;
import org.com.smartpayments.subscription.core.domain.core.usecase.purchaseCharge.createPurchaseCharge.CreatePurchaseChargeUsecase;
import org.com.smartpayments.subscription.core.domain.enums.EPlan;
import org.com.smartpayments.subscription.core.domain.enums.EPurchaseChargeStatus;
import org.com.smartpayments.subscription.core.domain.enums.EPurchaseStatus;
import org.com.smartpayments.subscription.core.domain.enums.EPurchaseType;
import org.com.smartpayments.subscription.core.domain.model.Plan;
import org.com.smartpayments.subscription.core.domain.model.Purchase;
import org.com.smartpayments.subscription.core.domain.model.PurchaseItem;
import org.com.smartpayments.subscription.core.domain.model.User;
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

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewSubscriptionPurchaseUsecase implements UsecasePort<NewSubscriptionPurchaseInput, NewSubscriptionPurchaseOutput> {
    private final UserDataProviderPort userDataProviderPort;
    private final PurchaseDataProviderPort purchaseDataProviderPort;
    private final PlanDataProviderPort planDataProviderPort;

    private final PaymentGatewaySubscriptionClientPort paymentGatewaySubscriptionClientPort;
    private final PaymentGatewayUserClientPort paymentGatewayUserClientPort;

    private final CreatePurchaseChargeUsecase createPurchaseChargeUsecase;

    @Override
    @Transactional
    public NewSubscriptionPurchaseOutput execute(NewSubscriptionPurchaseInput input) {
        NewSubscriptionPurchaseValidator.validate(input);

        User user = findUser(input.getUserId());
        boolean willCreateUserExternal = isEmpty(user.getUserPaymentGatewayExternalId());

        user = ensureUserExistsPaymentGateway(user, willCreateUserExternal);
        Purchase purchase = null;

        try {
            purchase = fillPurchase(user, input);
            purchase = persistPurchase(purchase);

            CreateSubscriptionOutput subscriptionChargeOutput = createSubscriptionCharge(user, purchase);
            purchase.setExternalId(subscriptionChargeOutput.getId());
            String paymentUrl = findSubscriptionChargeFirstPaymentUrl(user, purchase);

            persistPurchase(purchase);

            return new NewSubscriptionPurchaseOutput(paymentUrl);
        } catch (Exception e) {
            if (willCreateUserExternal && !isEmpty(user.getUserPaymentGatewayExternalId())) {
                paymentGatewayUserClientPort.deleteUserClient(user);
            }

            if (nonNull(purchase) && nonNull(purchase.getExternalId())) {
                paymentGatewaySubscriptionClientPort.deleteSubscription(user, purchase.getExternalId());
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

    private void handlePlanPurchased(Purchase purchase, List<PurchaseItem> purchaseItems, NewSubscriptionPurchaseInput.PurchaseItemInput purchasedPlan) {
        Plan plan = planDataProviderPort.findById(purchasedPlan.getPlanId())
            .orElseThrow(() -> new PlanNotFoundException(purchasedPlan.getPlanId()));

        if (plan.getType().equals(EPlan.FREE)) {
            throw new InvalidSubscriptionException("Can't purchase FREE plan!");
        }

        purchaseItems.add(PurchaseItem.builder()
            .description(mountPurchaseItemDescription("PLAN_" + plan.getType(), 1, plan.getValue()))
            .quantity(1)
            .value(plan.getValue())
            .plan(plan)
            .purchase(purchase)
            .build()
        );
    }

    private void fillPurchaseItems(Purchase purchase, NewSubscriptionPurchaseInput input) {
        input.getPurchaseItems().stream().filter(pi -> nonNull(pi.getPlanId()))
            .findFirst().ifPresent(purchaseItemInput -> {
                //todo: validate if user has active plan
                handlePlanPurchased(purchase, purchase.getItems(), purchaseItemInput);
            });
    }

    private BigDecimal sumPurchaseTotal(List<PurchaseItem> items) {
        return items.stream()
            .map(purchaseItem -> purchaseItem.getValue().multiply(BigDecimal.valueOf(purchaseItem.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Purchase fillPurchase(User user, NewSubscriptionPurchaseInput input) {
        Purchase purchase = Purchase.builder()
            .user(user)
            .paymentMethod(input.getPaymentMethod())
            .totalValue(BigDecimal.ZERO)
            .status(EPurchaseStatus.PENDING)
            .purchaseType(EPurchaseType.SUBSCRIPTION)
            .installments(input.getInstallments())
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

    private CreateSubscriptionOutput createSubscriptionCharge(User user, Purchase purchase) {
        PurchaseItem purchasePlanTotalValue = purchase.getItems()
            .stream().filter(puri -> nonNull(puri.getPlan())).findFirst()
            .orElseThrow(() -> new PlanNotFoundException("Plan not found while creating external service subscription charge!"));

        NewSubscriptionClientInput input = NewSubscriptionClientInput.builder()
            .customer(user.getUserPaymentGatewayExternalId())
            .billingType(purchase.getPaymentMethod().name())
            .value(purchasePlanTotalValue.getValue())
            .nextDueDate(new Date())
            .description(mountChargeDescription(purchase.getItems()))
            .externalReference(purchase.getId().toString())
            .build();

        return paymentGatewaySubscriptionClientPort.createSubscription(user, input);
    }

    private String findSubscriptionChargeFirstPaymentUrl(User user, Purchase purchase) {
        GetSubscriptionChargesOutput subscriptionCharges = paymentGatewaySubscriptionClientPort.getSubscriptionCharges(user, purchase.getExternalId());

        if (subscriptionCharges.getData().isEmpty()) {
            throw new SubscriptionChargeNotFoundException();
        }

        GetSubscriptionChargesOutput.DataOutput firstCharge = subscriptionCharges.getData().getFirst();

        persistFirstPurchaseCharge(purchase, firstCharge);

        return isNull(firstCharge.getInvoiceUrl()) ? firstCharge.getBankSlipUrl() : firstCharge.getInvoiceUrl();
    }

    private void persistFirstPurchaseCharge(Purchase purchase, GetSubscriptionChargesOutput.DataOutput input) {
        CreatePurchaseChargeInput createChargeInput = CreatePurchaseChargeInput.builder()
            .purchase(purchase)
            .externalChargeId(input.getId())
            .value(input.getValue())
            .description(input.getDescription())
            .billingType(input.getBillingType())
            .pixTransaction(input.getPixTransaction())
            .pixQrCodeId(input.getPixQrCodeId())
            .status(EPurchaseChargeStatus.valueOf(input.getStatus()))
            .dueDate(input.getDueDate())
            .paymentDate(input.getPaymentDate())
            .invoiceUrl(input.getInvoiceUrl())
            .bankSlipUrl(input.getBankSlipUrl())
            .build();

        createPurchaseChargeUsecase.execute(createChargeInput);
    }
}
