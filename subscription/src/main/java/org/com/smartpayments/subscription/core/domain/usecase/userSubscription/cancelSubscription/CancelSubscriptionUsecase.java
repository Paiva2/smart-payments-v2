package org.com.smartpayments.subscription.core.domain.usecase.userSubscription.cancelSubscription;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.smartpayments.subscription.core.common.exception.GenericException;
import org.com.smartpayments.subscription.core.common.exception.PlanNotFoundException;
import org.com.smartpayments.subscription.core.common.exception.UserSubscriptionNotFoundException;
import org.com.smartpayments.subscription.core.common.exception.UserSubscriptionResumeViewNotFoundException;
import org.com.smartpayments.subscription.core.domain.enums.EPlan;
import org.com.smartpayments.subscription.core.domain.enums.ESubscriptionRecurrence;
import org.com.smartpayments.subscription.core.domain.enums.ESubscriptionStatus;
import org.com.smartpayments.subscription.core.domain.model.Plan;
import org.com.smartpayments.subscription.core.domain.model.User;
import org.com.smartpayments.subscription.core.domain.model.UserSubscription;
import org.com.smartpayments.subscription.core.domain.model.views.UserSubscriptionResumeView;
import org.com.smartpayments.subscription.core.domain.usecase.userSubscription.cancelSubscription.event.SubscriptionCancelledEvent;
import org.com.smartpayments.subscription.core.domain.usecase.userSubscription.cancelSubscription.exception.InvalidCancellationException;
import org.com.smartpayments.subscription.core.domain.usecase.userSubscription.cancelSubscription.exception.SubscriptionAlreadyCancelledException;
import org.com.smartpayments.subscription.core.ports.in.UsecaseVoidPort;
import org.com.smartpayments.subscription.core.ports.in.utils.MessageUtilsPort;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.*;
import org.com.smartpayments.subscription.core.ports.out.dto.AsyncEmailOutput;
import org.com.smartpayments.subscription.core.ports.out.dto.AsyncMessageOutput;
import org.com.smartpayments.subscription.core.ports.out.dto.AsyncUserSubscriptionUpdateOutput;
import org.com.smartpayments.subscription.core.ports.out.external.dto.DeleteSubscriptionOutput;
import org.com.smartpayments.subscription.core.ports.out.external.paymentGateway.PaymentGatewaySubscriptionClientPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.*;

import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j
@Service
@RequiredArgsConstructor
public class CancelSubscriptionUsecase implements UsecaseVoidPort<Long> {
    private final static ObjectMapper mapper = new ObjectMapper();
    private final static String SUBSCRIPTION_CANCELLED_TEMPLATE = "subscription-cancelled";
    private final static String SUBSCRIPTION_CANCELLED_SUBJECT = "Sua assinatura foi cancelada - Smart Payments";

    private final UserSubscriptionDataProviderPort userSubscriptionDataProviderPort;
    private final PaymentGatewaySubscriptionClientPort paymentGatewaySubscriptionClientPort;
    private final PlanDataProviderPort planDataProviderPort;
    private final UserSubscriptionCreditRecurrenceDataProviderPort userSubscriptionCreditRecurrenceDataProviderPort;
    private final UserSubscriptionCreditHistoryDataProviderPort userSubscriptionCreditHistoryDataProviderPort;
    private final UserSubscriptionResumeViewDataProviderPort userSubscriptionResumeViewDataProviderPort;

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final MessageUtilsPort messageUtilsPort;

    private final ApplicationEventPublisher applicationEventPublisher;

    @Value("${spring.kafka.topics.mail-sender}")
    private String SEND_EMAIL_TOPIC;

    @Value("${spring.kafka.topics.user-subscription-update}")
    private String UPDATE_USER_SUBSCRIPTION_TOPIC;

    @Override
    @Transactional
    public void execute(Long userId) {
        UserSubscription userSubscription = findUserSubscription(userId);

        validateSubscriptionCanBeCancelled(userSubscription);

        boolean willBeCancelledNow = userSubscription.getStatus().equals(ESubscriptionStatus.EXPIRED);
        List<AsyncEmailOutput> emailToNotifyCancelling = new ArrayList<>();

        if (willBeCancelledNow) {
            Plan freePlan = findFreePlan();
            emailToNotifyCancelling.add(mountAsyncEmail(userSubscription.getUser()));

            String externalSubscriptionId = userSubscription.getExternalSubscriptionId();

            setUserSubscriptionFree(freePlan, userSubscription);
            deleteSubscriptionExternal(externalSubscriptionId);
        } else {
            userSubscription.setStatus(ESubscriptionStatus.CANCELLED);
        }

        persistSubscriptionCancelled(userSubscription);

        emailToNotifyCancelling.forEach(this::sendUserEmailSubscriptionCancelled);

        // this is necessary to execute the message of subscription update after transaction is commited
        applicationEventPublisher.publishEvent(new SubscriptionCancelledEvent(userSubscription.getUser()));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSubscriptionCancelled(SubscriptionCancelledEvent event) {
        sendUserSubscriptionUpdateMessage(event.getUser());
    }

    private UserSubscription findUserSubscription(Long userId) {
        return userSubscriptionDataProviderPort.findByUserWithPlan(userId)
            .orElseThrow(UserSubscriptionNotFoundException::new);
    }

    private void validateSubscriptionCanBeCancelled(UserSubscription userSubscription) {
        if (userSubscription.getPlan().getType().equals(EPlan.FREE)) {
            throw new InvalidCancellationException("Can't cancel a free subscription!");
        }

        if (userSubscription.getStatus().equals(ESubscriptionStatus.CANCELLED)) {
            throw new SubscriptionAlreadyCancelledException();
        }
    }

    private Plan findFreePlan() {
        return planDataProviderPort.findByType(EPlan.FREE)
            .orElseThrow(() -> new PlanNotFoundException("Plan not found!"));
    }

    private void deleteSubscriptionExternal(String externalSubscriptionId) {
        if (isEmpty(externalSubscriptionId)) return;

        DeleteSubscriptionOutput output = paymentGatewaySubscriptionClientPort.deleteSubscription(externalSubscriptionId);

        if (!output.isDeleted()) {
            log.error("[CancelSubscriptionUsecase#deleteSubscriptionExternal] - Error while removing subscription external!");
            throw new InvalidCancellationException("Error while cancelling subscription!");
        }
    }

    private void setUserSubscriptionFree(Plan freePlan, UserSubscription userSubscription) {
        userSubscription.setNextPaymentDate(null);
        userSubscription.setExternalSubscriptionId(null);
        userSubscription.setRecurrence(null);
        userSubscription.setExpiredAt(null);
        userSubscription.setStatus(ESubscriptionStatus.ACTIVE);
        userSubscription.setValue(freePlan.getValue());
        userSubscription.setPlan(freePlan);

        userSubscription.setUnlimitedSmsCredits(freePlan.getUnlimitedSmsCredits());
        userSubscription.setUnlimitedWhatsAppCredits(freePlan.getUnlimitedWhatsAppCredits());
        userSubscription.setUnlimitedEmailCredits(freePlan.getUnlimitedEmailCredits());

        removeRecurrentCredits(userSubscription);
        expireCurrentPeriodCredits(userSubscription);
    }

    private void removeRecurrentCredits(UserSubscription userSubscription) {
        userSubscriptionCreditRecurrenceDataProviderPort.deleteAllByUserSubscription(userSubscription.getId());
    }

    private void expireCurrentPeriodCredits(UserSubscription userSubscription) {
        userSubscriptionCreditHistoryDataProviderPort.revokeNonExpiredCredits(userSubscription.getId());
    }

    private void persistSubscriptionCancelled(UserSubscription userSubscription) {
        userSubscriptionDataProviderPort.persist(userSubscription);
    }

    private Map<String, Object> fillEmailVariables(User user) {
        return new HashMap<>() {{
            put("${FIRST_NAME}", user.getFirstName());
        }};
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

    private void sendUserEmailSubscriptionCancelled(AsyncEmailOutput asyncEmail) {
        try {
            kafkaTemplate.send(SEND_EMAIL_TOPIC, mapper.writeValueAsString(asyncEmail));
        } catch (JsonProcessingException e) {
            String message = "Error while sending email!";
            log.error(message, e);
            throw new GenericException(message);
        }
    }

    private void sendUserSubscriptionUpdateMessage(User user) {
        UserSubscriptionResumeView userSubscriptionResumeView = userSubscriptionResumeViewDataProviderPort
            .findByUser(user.getId()).orElseThrow(UserSubscriptionResumeViewNotFoundException::new);

        try {
            String messageIssuer = "SUBSCRIPTION";

            AsyncUserSubscriptionUpdateOutput message = AsyncUserSubscriptionUpdateOutput.builder()
                .userId(userSubscriptionResumeView.getUserId())
                .plan(isEmpty(userSubscriptionResumeView.getPlan()) ? null : EPlan.valueOf(userSubscriptionResumeView.getPlan()))
                .status(isEmpty(userSubscriptionResumeView.getStatus()) ? null : ESubscriptionStatus.valueOf(userSubscriptionResumeView.getStatus()))
                .nextPaymentDate(isEmpty(userSubscriptionResumeView.getNextPaymentDate()) ? null : userSubscriptionResumeView.getNextPaymentDate().toString())
                .recurrence(isEmpty(userSubscriptionResumeView.getRecurrence()) ? null : ESubscriptionRecurrence.valueOf(userSubscriptionResumeView.getRecurrence()))
                .value(userSubscriptionResumeView.getValue())
                .unlimitedEmailCredits(userSubscriptionResumeView.getUnlimitedEmailCredits())
                .emailCredits(userSubscriptionResumeView.getEmailCredits())
                .unlimitedWhatsAppCredits(userSubscriptionResumeView.getUnlimitedWhatsappCredits())
                .whatsAppCredits(userSubscriptionResumeView.getWhatsappCredits())
                .unlimitedSmsCredits(userSubscriptionResumeView.getUnlimitedSmsCredits())
                .smsCredits(userSubscriptionResumeView.getSmsCredits())
                .build();

            AsyncMessageOutput<Object> asyncMessage = AsyncMessageOutput.builder()
                .messageHash(messageUtilsPort.generateMessageHash(messageIssuer))
                .timestamp(new Date())
                .issuer(messageIssuer)
                .data(message)
                .build();

            kafkaTemplate.send(UPDATE_USER_SUBSCRIPTION_TOPIC, mapper.writeValueAsString(asyncMessage));
        } catch (Exception e) {
            String message = "Error while sending user subscription update message!";
            log.error(message, e);
            throw new GenericException(message);
        }
    }
}
