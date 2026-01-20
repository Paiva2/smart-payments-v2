package org.com.smartpayments.subscription.core.domain.usecase.userSubscription.sendUserSubscriptionUpdateMessage;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.smartpayments.subscription.core.common.exception.GenericException;
import org.com.smartpayments.subscription.core.common.exception.UserSubscriptionResumeViewNotFoundException;
import org.com.smartpayments.subscription.core.domain.enums.EPlan;
import org.com.smartpayments.subscription.core.domain.enums.ESubscriptionRecurrence;
import org.com.smartpayments.subscription.core.domain.enums.ESubscriptionStatus;
import org.com.smartpayments.subscription.core.domain.model.User;
import org.com.smartpayments.subscription.core.domain.model.views.UserSubscriptionResumeView;
import org.com.smartpayments.subscription.core.ports.in.UsecaseVoidPort;
import org.com.smartpayments.subscription.core.ports.in.utils.MessageUtilsPort;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.UserSubscriptionResumeViewDataProviderPort;
import org.com.smartpayments.subscription.core.ports.out.dto.AsyncMessageOutput;
import org.com.smartpayments.subscription.core.ports.out.dto.AsyncUserSubscriptionUpdateOutput;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;

import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j
@Service
@RequiredArgsConstructor
public class SendUserSubscriptionUpdateMessageUsecase implements UsecaseVoidPort<User> {
    private final static ObjectMapper mapper = new ObjectMapper();

    private final UserSubscriptionResumeViewDataProviderPort userSubscriptionResumeViewDataProviderPort;

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final MessageUtilsPort messageUtilsPort;

    @Value("${spring.kafka.topics.user-subscription-update}")
    private String UPDATE_USER_SUBSCRIPTION_TOPIC;

    @Override
    public void execute(User user) {
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
                .subscriptionEmailCredits(userSubscriptionResumeView.getSubscriptionEmailCredits())
                .subscriptionSmsCredits(userSubscriptionResumeView.getSubscriptionSmsCredits())
                .subscriptionWhatsAppCredits(userSubscriptionResumeView.getSubscriptionWhatsAppCredits())
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
