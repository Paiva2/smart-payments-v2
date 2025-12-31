package org.com.smartpayments.subscription.core.domain.usecase.userSubscription.revokeUserSubscription;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.smartpayments.subscription.core.common.exception.GenericException;
import org.com.smartpayments.subscription.core.common.exception.PlanNotFoundException;
import org.com.smartpayments.subscription.core.common.exception.UserSubscriptionNotFoundException;
import org.com.smartpayments.subscription.core.domain.enums.EPlan;
import org.com.smartpayments.subscription.core.domain.enums.ESubscriptionStatus;
import org.com.smartpayments.subscription.core.domain.enums.EUserSubscriptionState;
import org.com.smartpayments.subscription.core.domain.model.Plan;
import org.com.smartpayments.subscription.core.domain.model.User;
import org.com.smartpayments.subscription.core.domain.model.UserSubscription;
import org.com.smartpayments.subscription.core.ports.in.UsecaseVoidPort;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.PlanDataProviderPort;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.UserSubscriptionCreditHistoryDataProviderPort;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.UserSubscriptionCreditRecurrenceDataProviderPort;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.UserSubscriptionDataProviderPort;
import org.com.smartpayments.subscription.core.ports.out.dto.AsyncEmailOutput;
import org.com.smartpayments.subscription.core.ports.out.dto.AsyncSubscriptionPlanStateInput;
import org.com.smartpayments.subscription.core.ports.out.external.paymentGateway.PaymentGatewaySubscriptionClientPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.nonNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class RevokeUserSubscriptionUsecase implements UsecaseVoidPort<AsyncSubscriptionPlanStateInput> {
    private final static ObjectMapper mapper = new ObjectMapper();
    private final static String SUBSCRIPTION_CANCELLED_TEMPLATE = "subscription-cancelled";
    private final static String SUBSCRIPTION_CANCELLED_SUBJECT = "Sua assinatura foi cancelada - Smart Payments";

    private final UserSubscriptionDataProviderPort userSubscriptionDataProviderPort;
    private final UserSubscriptionCreditRecurrenceDataProviderPort userSubscriptionCreditRecurrenceDataProviderPort;
    private final UserSubscriptionCreditHistoryDataProviderPort userSubscriptionCreditHistoryDataProviderPort;
    private final PlanDataProviderPort planDataProviderPort;
    private final PaymentGatewaySubscriptionClientPort paymentGatewaySubscriptionClientPort;

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${spring.kafka.topics.mail-sender}")
    private String SEND_EMAIL_TOPIC;

    @Override
    @Transactional
    public void execute(AsyncSubscriptionPlanStateInput input) {
        UserSubscription userSubscription = findUserSubscription(input.getUserSubscriptionId());

        log.info("[RevokeUserSubscriptionUsecase#execute] - Starting subscription revoke. User Subscription Id: {}", userSubscription.getId());

        String subscriptionExternalId = userSubscription.getExternalSubscriptionId();

        setUserSubscriptionPlanFree(userSubscription);
        deleteCreditRecurrences(userSubscription);
        invalidateCurrentCredits(userSubscription);

        if (nonNull(subscriptionExternalId)) {
            deleteSubscriptionPaymentGateway(userSubscription.getUser(), subscriptionExternalId);
        }

        if (input.getState().equals(EUserSubscriptionState.CANCELLED)) {
            AsyncEmailOutput cancelledEmail = mountAsyncEmail(userSubscription.getUser());
            sendUserEmailSubscriptionCancelled(cancelledEmail);
        }

        log.info("[RevokeUserSubscriptionUsecase#execute] - Subscription revoked. User Subscription Id: {}", userSubscription.getId());
    }

    private UserSubscription findUserSubscription(Long userSubscriptionId) {
        return userSubscriptionDataProviderPort.findById(userSubscriptionId)
            .orElseThrow(UserSubscriptionNotFoundException::new);
    }

    private void setUserSubscriptionPlanFree(UserSubscription userSubscription) {
        Plan freePlan = findFreePlan();

        userSubscription.setExpiredAt(null);
        userSubscription.setStatus(ESubscriptionStatus.ACTIVE);
        userSubscription.setExternalSubscriptionId(null);
        userSubscription.setPlan(freePlan);
        userSubscription.setValue(BigDecimal.ZERO);
        userSubscription.setRecurrence(null);
        userSubscription.setNextPaymentDate(null);
        userSubscription.setUnlimitedWhatsAppCredits(freePlan.getUnlimitedWhatsAppCredits());
        userSubscription.setUnlimitedEmailCredits(freePlan.getUnlimitedEmailCredits());
        userSubscription.setUnlimitedSmsCredits(freePlan.getUnlimitedSmsCredits());

        userSubscriptionDataProviderPort.persist(userSubscription);
    }

    private void deleteCreditRecurrences(UserSubscription userSubscription) {
        userSubscriptionCreditRecurrenceDataProviderPort.deleteAllByUserSubscription(userSubscription.getId());
    }

    private void invalidateCurrentCredits(UserSubscription userSubscription) {
        userSubscriptionCreditHistoryDataProviderPort.revokeNonExpiredCredits(userSubscription.getId());
    }

    private Plan findFreePlan() {
        return planDataProviderPort.findByType(EPlan.FREE)
            .orElseThrow(() -> new PlanNotFoundException("Plan not found! Plan: " + EPlan.FREE));
    }

    private void deleteSubscriptionPaymentGateway(User user, String subscriptionExternalId) {
        paymentGatewaySubscriptionClientPort.deleteSubscription(user, subscriptionExternalId);
    }

    private AsyncEmailOutput mountAsyncEmail(User user) {
        return AsyncEmailOutput.builder()
            .to(user.getEmail())
            .templateName(SUBSCRIPTION_CANCELLED_TEMPLATE)
            .subject(SUBSCRIPTION_CANCELLED_SUBJECT)
            .cc(new ArrayList<>())
            .variables(fillEmailVariables(user))
            .build();
    }

    private Map<String, Object> fillEmailVariables(User user) {
        return new HashMap<>() {{
            put("${FIRST_NAME}", user.getFirstName());
        }};
    }

    private void sendUserEmailSubscriptionCancelled(AsyncEmailOutput asyncEmail) {
        try {
            kafkaTemplate.send(SEND_EMAIL_TOPIC, mapper.writeValueAsString(asyncEmail));
        } catch (JsonProcessingException e) {
            String message = "Error while sending email!";
            log.error(message, e);
            throw new GenericException(message);
        }
    }
}
