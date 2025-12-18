package org.com.smartpayments.subscription.core.domain.usecase.purchaseCharge.confirmPurchaseCharge;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.smartpayments.subscription.core.common.exception.CreditNotFoundException;
import org.com.smartpayments.subscription.core.common.exception.GenericException;
import org.com.smartpayments.subscription.core.common.exception.PurchaseNotFoundException;
import org.com.smartpayments.subscription.core.domain.enums.ECredit;
import org.com.smartpayments.subscription.core.domain.enums.ECreditTransactionType;
import org.com.smartpayments.subscription.core.domain.enums.EPlan;
import org.com.smartpayments.subscription.core.domain.enums.EPurchaseStatus;
import org.com.smartpayments.subscription.core.domain.enums.ESubscriptionRecurrence;
import org.com.smartpayments.subscription.core.domain.enums.ESubscriptionStatus;
import org.com.smartpayments.subscription.core.domain.model.Credit;
import org.com.smartpayments.subscription.core.domain.model.Plan;
import org.com.smartpayments.subscription.core.domain.model.Purchase;
import org.com.smartpayments.subscription.core.domain.model.PurchaseItem;
import org.com.smartpayments.subscription.core.domain.model.User;
import org.com.smartpayments.subscription.core.domain.model.UserSubscription;
import org.com.smartpayments.subscription.core.domain.model.UserSubscriptionCreditHistory;
import org.com.smartpayments.subscription.core.domain.model.UserSubscriptionCreditRecurrence;
import org.com.smartpayments.subscription.core.domain.usecase.purchaseCharge.confirmPurchaseCharge.exception.InvalidPurchaseException;
import org.com.smartpayments.subscription.core.domain.usecase.purchaseCharge.confirmPurchaseCharge.exception.MissingCreditPurchaseItemException;
import org.com.smartpayments.subscription.core.domain.usecase.purchaseCharge.confirmPurchaseCharge.exception.UserAlreadyHasActivePlanException;
import org.com.smartpayments.subscription.core.ports.in.UsecaseVoidPort;
import org.com.smartpayments.subscription.core.ports.in.dto.PurchaseChargeConfirmedInput;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.CreditDataProviderPort;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.PurchaseChargeDataProviderPort;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.PurchaseDataProviderPort;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.UserSubscriptionCreditHistoryDataProviderPort;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.UserSubscriptionCreditRecurrenceDataProviderPort;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.UserSubscriptionDataProviderPort;
import org.com.smartpayments.subscription.core.ports.out.dto.AsyncEmailOutput;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfirmPurchaseChargeUsecase implements UsecaseVoidPort<PurchaseChargeConfirmedInput> {
    private final static ObjectMapper mapper = new ObjectMapper();

    private final static String PURCHASE_CONFIRMED_TEMPLATE = "purchase-confirmed";
    private final static String PURCHASE_CONFIRMED_SUBJECT = "Pagamento confirmado";

    private final PurchaseDataProviderPort purchaseDataProviderPort;
    private final PurchaseChargeDataProviderPort purchaseChargeDataProviderPort;
    private final UserSubscriptionDataProviderPort userSubscriptionDataProviderPort;
    private final UserSubscriptionCreditHistoryDataProviderPort userSubscriptionCreditHistoryDataProviderPort;
    private final CreditDataProviderPort creditDataProviderPort;
    private final UserSubscriptionCreditRecurrenceDataProviderPort userSubscriptionCreditRecurrenceDataProviderPort;

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${spring.kafka.topics.mail-sender}")
    private String SEND_EMAIL_TOPIC;

    @Override
    @Transactional
    public void execute(PurchaseChargeConfirmedInput input) {
        Purchase purchase = findPurchase(input.getExternalPurchaseId());

        if (findPurchaseChargeConfirmed(input.getExternalChargeId())) {
            log.info("[ConfirmPurchaseChargeUsecase#execute] - Charge already confirmed, ignoring. id: {}", input.getExternalChargeId());
            return;
        }

        List<AsyncEmailOutput> asyncEmail = new ArrayList<>();

        User user = purchase.getUser();

        if (input.isFromSubscription()) {
            handleSubscriptionPaymentConfirmed(user, input.getDueDate(), purchase, asyncEmail);
        } else {
            handleCreditPaymentConfirmed(purchase, asyncEmail);
        }

        updatePurchaseChargeConfirmed(input.getExternalChargeId(), input.getPaymentDate());
        updatePurchaseConfirmed(purchase);

        asyncEmail.forEach(this::sendUserEmailChargeConfirmed);
    }

    private Purchase findPurchase(String externalPurchaseId) {
        return purchaseDataProviderPort.findByExternalIdLocking(externalPurchaseId)
            .orElseThrow(() -> new PurchaseNotFoundException("Purchase not found!"));
    }

    private boolean findPurchaseChargeConfirmed(String externalChargeId) {
        return purchaseChargeDataProviderPort.isConfirmedByExternalId(externalChargeId);
    }

    private void handleCreditPaymentConfirmed(Purchase purchase, List<AsyncEmailOutput> asyncEmail) {
        List<PurchaseItem> purchasedItems = purchase.getItems();
        List<UserSubscriptionCreditHistory> userSubscriptionCreditHistoriesToSave = new ArrayList<>();

        for (PurchaseItem purchaseItem : purchasedItems) {
            if (isNull(purchaseItem.getCredit())) {
                throw new MissingCreditPurchaseItemException("Purchase item has an invalid credit id. Purchase item id: " + purchaseItem.getId());
            }

            Credit creditPurchased = purchaseItem.getCredit();
            int quantity = purchaseItem.getQuantity();

            if (purchaseItem.getQuantity() < 1) {
                throw new MissingCreditPurchaseItemException("Purchase item has an invalid quantity: " + purchaseItem.getQuantity());
            }

            userSubscriptionCreditHistoriesToSave.add(
                fillCreditHistory(purchase.getUser().getSubscription(), creditPurchased.getType(), quantity, null)
            );
        }

        userSubscriptionCreditHistoryDataProviderPort.persistAll(userSubscriptionCreditHistoriesToSave);
        asyncEmail.add(mountAsyncEmail(purchase.getUser(), mountItemsResumeEmail(purchase), false));
    }

    private UserSubscriptionCreditHistory fillCreditHistory(UserSubscription userSubscription, ECredit credit, int quantity, Date expiresAt) {
        return UserSubscriptionCreditHistory.builder()
            .amount(quantity)
            .creditType(credit)
            .transactionType(ECreditTransactionType.GRANT)
            .validFrom(new Date())
            .expiresAt(expiresAt)
            .userSubscription(userSubscription)
            .build();
    }

    private void handleSubscriptionPaymentConfirmed(User user, Date chargeDueDate, Purchase purchase, List<AsyncEmailOutput> asyncEmail) {
        UserSubscription userSubscription = user.getSubscription();
        boolean hasNoNextPaymentDate = isNull(userSubscription.getNextPaymentDate());

        if (purchase.getItems().isEmpty()) {
            throw new InvalidPurchaseException("Purchase has no items!");
        }

        PurchaseItem purchasedItem = purchase.getItems().getFirst();

        if (isNull(purchasedItem.getPlan())) {
            throw new InvalidPurchaseException("Purchase item has no plan associated!");
        }

        if (!userSubscription.getPlan().getType().equals(EPlan.FREE) && hasNoNextPaymentDate) {
            throw new UserAlreadyHasActivePlanException("User already has an active subscription. Can't active another while having one active!");
        }

        Date nextPaymentDate = defineNextPaymentDate(chargeDueDate);

        // First Activation
        if (hasNoNextPaymentDate && isNull(userSubscription.getExternalSubscriptionId())) {
            log.info("[ConfirmPurchaseChargeUsecase#execute] Activating plan for user. UserId: {}, PlanType: {}, ExternalSubId: {}",
                userSubscription.getUser().getId(),
                purchasedItem.getPlan().getType(),
                purchase.getExternalId()
            );

            Plan planPurchased = purchasedItem.getPlan();

            userSubscription.setPlan(planPurchased);
            userSubscription.setExternalSubscriptionId(purchase.getExternalId());
            userSubscription.setRecurrence(ESubscriptionRecurrence.MONTHLY);
            userSubscription.setUnlimitedEmailCredits(planPurchased.getUnlimitedEmailCredits());
            userSubscription.setUnlimitedSmsCredits(planPurchased.getUnlimitedSmsCredits());
            userSubscription.setUnlimitedWhatsAppCredits(planPurchased.getUnlimitedWhatsAppCredits());
            userSubscription.setValue(planPurchased.getValue());

            removeSampleCreditsFromHistory(userSubscription);
            addSubscriptionCreditsAndRecurrences(planPurchased, userSubscription, nextPaymentDate);

            asyncEmail.add(mountAsyncEmail(purchase.getUser(), mountItemsResumeEmail(purchase), true));
        } else if (!Objects.equals(userSubscription.getExternalSubscriptionId(), purchase.getExternalId())) {
            throw new UserAlreadyHasActivePlanException("Can't confirm a payment of another subscription with an subscription already active!");
        } else {
            log.info("[ConfirmPurchaseChargeUsecase#execute] Renewing subscription. UserId: {}, NextPayment: {}",
                userSubscription.getUser().getId(),
                userSubscription.getNextPaymentDate()
            );
        }

        userSubscription.setNextPaymentDate(nextPaymentDate);
        userSubscription.setStatus(ESubscriptionStatus.ACTIVE);

        persistUserSubscription(userSubscription);
    }

    private void removeSampleCreditsFromHistory(UserSubscription userSubscription) {
        userSubscriptionCreditHistoryDataProviderPort.revokeSampleCredits(userSubscription.getId());
    }

    private void addSubscriptionCreditsAndRecurrences(Plan planPurchased, UserSubscription userSubscription, Date nextPaymentDate) {
        Date creditExp = defineSubscriptionCreditsExp(nextPaymentDate);

        List<UserSubscriptionCreditHistory> credits = new ArrayList<>();
        List<UserSubscriptionCreditRecurrence> recurrences = new ArrayList<>();

        if (planPurchased.getWhatsAppCreditsQuantity() > 0 && !planPurchased.getUnlimitedWhatsAppCredits()) {
            credits.add(fillCreditHistory(userSubscription, ECredit.WHATS_APP, planPurchased.getWhatsAppCreditsQuantity(), creditExp));
            recurrences.add(fillCreditRecurrence(planPurchased.getWhatsAppCreditsQuantity(), userSubscription, ECredit.WHATS_APP));
        }

        if (planPurchased.getSmsCreditsQuantity() > 0 && !planPurchased.getUnlimitedSmsCredits()) {
            credits.add(fillCreditHistory(userSubscription, ECredit.SMS, planPurchased.getWhatsAppCreditsQuantity(), creditExp));
            recurrences.add(fillCreditRecurrence(planPurchased.getSmsCreditsQuantity(), userSubscription, ECredit.SMS));
        }

        if (planPurchased.getEmailCreditsQuantity() > 0 && !planPurchased.getUnlimitedEmailCredits()) {
            credits.add(fillCreditHistory(userSubscription, ECredit.EMAIL, planPurchased.getWhatsAppCreditsQuantity(), creditExp));
            recurrences.add(fillCreditRecurrence(planPurchased.getSmsCreditsQuantity(), userSubscription, ECredit.EMAIL));
        }

        userSubscriptionCreditHistoryDataProviderPort.persistAll(credits);
        userSubscriptionCreditRecurrenceDataProviderPort.persistAll(recurrences);
    }

    private Date defineSubscriptionCreditsExp(Date nextPaymentDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(nextPaymentDate);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private UserSubscriptionCreditRecurrence fillCreditRecurrence(int quantity, UserSubscription userSubscription, ECredit credit) {
        Credit creditRecurrent = findCredit(credit);

        return UserSubscriptionCreditRecurrence.builder()
            .id(new UserSubscriptionCreditRecurrence.KeyId(userSubscription.getId(), creditRecurrent.getId()))
            .quantity(quantity)
            .active(true)
            .userSubscription(userSubscription)
            .credit(creditRecurrent)
            .build();
    }

    private Credit findCredit(ECredit credit) {
        return creditDataProviderPort.findActiveByType(credit)
            .orElseThrow(() -> new CreditNotFoundException("Credit not found: " + credit));
    }

    private void persistUserSubscription(UserSubscription userSubscription) {
        userSubscriptionDataProviderPort.persist(userSubscription);
    }

    private Date defineNextPaymentDate(Date dueDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dueDate);
        calendar.add(Calendar.MONTH, 1);
        return calendar.getTime();
    }

    private void updatePurchaseChargeConfirmed(String chargeExternalId, Date paymentDate) {
        purchaseChargeDataProviderPort.confirmPurchaseChargePayment(chargeExternalId, paymentDate);
    }

    private void updatePurchaseConfirmed(Purchase purchase) {
        purchase.setStatus(EPurchaseStatus.CONFIRMED);
        purchaseDataProviderPort.persist(purchase);
    }

    private Map<String, Object> fillEmailVariables(User user, String items, boolean isSubscription) {
        String leadMessage = isSubscription ? "O plano já está ativo na sua conta e disponível para uso. Aproveite todos os benefícios do seu plano com as nossas funcionalidades!" :
            "Os créditos já estão disponíveis em sua conta e prontos para uso. Aproveite todos os seus créditos com as nossas funcionalidades!";

        String purchaseType = isSubscription ? "do seu plano" : "dos seus créditos";

        return new HashMap<>() {{
            put("${FIRST_NAME}", user.getFirstName());
            put("${PURCHASE_TYPE}", purchaseType);
            put("${PURCHASE_ITEMS_LI}", items);
            put("${LEAD_MESSAGE}", leadMessage);
        }};
    }

    private String formatCurrencyValue(BigDecimal value) {
        return NumberFormat.getCurrencyInstance(Locale.of("pt", "BR")).format(value);
    }

    private String mountItemsResumeEmail(Purchase purchase) {
        StringBuilder sb = new StringBuilder();

        for (PurchaseItem purchaseItem : purchase.getItems()) {
            boolean isSubscription = nonNull(purchaseItem.getPlan());

            sb.append("<li>")
                .append(isSubscription ? purchaseItem.getPlan().getType() : purchaseItem.getCredit().getType())
                .append(" - ")
                .append("Qtd: ").append(purchaseItem.getQuantity())
                .append(" - ")
                .append(isSubscription ? "Plano de Assinatura Mensal" : "Créditos")
                .append(" - ")
                .append("Valor: ").append(formatCurrencyValue(purchaseItem.getValue()))
                .append("</li>");
        }

        return sb.toString();
    }

    private AsyncEmailOutput mountAsyncEmail(User user, String items, boolean isSubscription) {
        return AsyncEmailOutput.builder()
            .to("joaovitor.paiva145@hotmail.com")
            .templateName(PURCHASE_CONFIRMED_TEMPLATE)
            .subject(PURCHASE_CONFIRMED_SUBJECT)
            .cc(new ArrayList<>())
            .variables(fillEmailVariables(user, items, isSubscription))
            .build();
    }

    private void sendUserEmailChargeConfirmed(AsyncEmailOutput asyncEmail) {
        try {
            kafkaTemplate.send(SEND_EMAIL_TOPIC, mapper.writeValueAsString(asyncEmail));
        } catch (JsonProcessingException e) {
            String message = "Error while sending email!";
            log.error(message, e);
            throw new GenericException(message);
        }
    }
}
