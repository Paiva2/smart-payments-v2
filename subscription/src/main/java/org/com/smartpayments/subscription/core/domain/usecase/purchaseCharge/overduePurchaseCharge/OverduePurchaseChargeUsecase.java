package org.com.smartpayments.subscription.core.domain.usecase.purchaseCharge.overduePurchaseCharge;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.smartpayments.subscription.core.domain.enums.ESubscriptionStatus;
import org.com.smartpayments.subscription.core.domain.model.User;
import org.com.smartpayments.subscription.core.domain.model.UserSubscription;
import org.com.smartpayments.subscription.core.ports.in.UsecasePort;
import org.com.smartpayments.subscription.core.ports.in.dto.PurchaseChargeOverdueInput;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.PurchaseChargeDataProviderPort;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.PurchaseDataProviderPort;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.UserSubscriptionDataProviderPort;
import org.com.smartpayments.subscription.core.ports.out.dto.AsyncEmailOutput;
import org.com.smartpayments.subscription.core.ports.out.external.paymentGateway.PaymentGatewaySubscriptionClientPort;
import org.com.smartpayments.subscription.core.ports.out.utils.DateUtilsPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OverduePurchaseChargeUsecase implements UsecasePort<PurchaseChargeOverdueInput, Boolean> {
    private final static ObjectMapper mapper = new ObjectMapper();
    private final static int MAX_DAYS_SUBSCRIPTION_EXPIRED = 2;

    private final static String SUBSCRIPTION_OVERDUE_TEMPLATE = "user-subscription-expired";
    private final static String SUBSCRIPTION_OVERDUE_SUBJECT = "Assinatura expirada";

    private final PurchaseDataProviderPort purchaseDataProviderPort;
    private final PurchaseChargeDataProviderPort purchaseChargeDataProviderPort;
    private final UserSubscriptionDataProviderPort userSubscriptionDataProviderPort;

    private final PaymentGatewaySubscriptionClientPort paymentGatewaySubscriptionClientPort;

    private final DateUtilsPort dateUtilsPort;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${spring.kafka.topics.mail-sender}")
    private String SEND_EMAIL_TOPIC;

    @Override
    @Transactional
    public Boolean execute(PurchaseChargeOverdueInput input) {
        updatePurchase(input);
        updateCharge(input);

        boolean shouldSendUpdate = false;

        if (!input.isFromSubscription()) {
            log.info("[OverduePurchaseChargeUsecase#execute] - Purchase expired for credit purchase! Purchase External Id: {}", input.getExternalPurchaseId());
            return shouldSendUpdate;
        }

        Optional<UserSubscription> userSubscription = findUserSubscription(input.getExternalPurchaseId());

        if (userSubscription.isPresent()) {
            if (userSubscription.get().getStatus().equals(ESubscriptionStatus.EXPIRED)) {
                log.info("[OverduePurchaseChargeUsecase#execute] - Subscription already expired. Message ignored! User subscription Id: {}", userSubscription.get().getId());
                return shouldSendUpdate;
            }

            userSubscription.get().setStatus(ESubscriptionStatus.EXPIRED);
            userSubscription.get().setExpiredAt(new Date());
            persistUserSubscription(userSubscription.get());
            sendEmailSubscriptionExpired(userSubscription.get());

            log.info("[OverduePurchaseChargeUsecase#execute] - Subscription expired. User notified! User subscription Id: {}", userSubscription.get().getId());
            shouldSendUpdate = true;
        } else {
            paymentGatewaySubscriptionClientPort.deleteSubscription(input.getExternalPurchaseId());
        }

        return shouldSendUpdate;
    }

    private void updatePurchase(PurchaseChargeOverdueInput input) {
        purchaseDataProviderPort.expirePurchase(input.getExternalPurchaseId());
    }

    private void persistUserSubscription(UserSubscription userSubscription) {
        userSubscriptionDataProviderPort.persist(userSubscription);
    }

    private void sendEmailSubscriptionExpired(UserSubscription userSubscription) {
        try {
            AsyncEmailOutput asyncEmail = mountAsyncEmail(userSubscription.getUser(), userSubscription.getExpiredAt());
            kafkaTemplate.send(SEND_EMAIL_TOPIC, mapper.writeValueAsString(asyncEmail));
        } catch (Exception e) {
            log.error("[OverduePurchaseChargeUsecase#sendEmailSubscriptionExpired] - Error while sending e-mail to user! User id: {}", userSubscription.getUser().getId(), e);
        }
    }

    private AsyncEmailOutput mountAsyncEmail(User user, Date expiredAt) {
        return AsyncEmailOutput.builder()
            .to(user.getEmail())
            .templateName(SUBSCRIPTION_OVERDUE_TEMPLATE)
            .subject(SUBSCRIPTION_OVERDUE_SUBJECT)
            .cc(new ArrayList<>())
            .variables(fillEmailVariables(user, expiredAt))
            .build();
    }

    private Map<String, Object> fillEmailVariables(User user, Date expiredAt) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(expiredAt);
        calendar.add(Calendar.DAY_OF_MONTH, MAX_DAYS_SUBSCRIPTION_EXPIRED);

        return new HashMap<>() {{
            put("${FIRST_NAME}", user.getFirstName());
            put("${MAX_DAYS_PAYMENT_SUBSCRIPTION}", MAX_DAYS_SUBSCRIPTION_EXPIRED + " dias");
            put("${SUBSCRIPTION_EXPIRES_AT}", dateUtilsPort.formatDate(calendar.getTime(), "dd/MM/yyyy"));
        }};
    }

    private void updateCharge(PurchaseChargeOverdueInput input) {
        purchaseChargeDataProviderPort.overduePurchaseChargePayment(input.getExternalChargeId());
    }

    private Optional<UserSubscription> findUserSubscription(String externalId) {
        return userSubscriptionDataProviderPort.findByExternalSubscriptionId(externalId);
    }
}
