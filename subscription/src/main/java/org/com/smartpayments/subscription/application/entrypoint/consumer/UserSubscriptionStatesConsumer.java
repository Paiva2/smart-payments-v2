package org.com.smartpayments.subscription.application.entrypoint.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.smartpayments.subscription.core.common.exception.GenericException;
import org.com.smartpayments.subscription.core.common.exception.UserNotFoundException;
import org.com.smartpayments.subscription.core.common.exception.UserSubscriptionResumeViewNotFoundException;
import org.com.smartpayments.subscription.core.domain.enums.EPlan;
import org.com.smartpayments.subscription.core.domain.enums.ESubscriptionRecurrence;
import org.com.smartpayments.subscription.core.domain.model.User;
import org.com.smartpayments.subscription.core.domain.model.views.UserSubscriptionResumeView;
import org.com.smartpayments.subscription.core.domain.usecase.userSubscription.renewUserSubscription.RenewUserSubscriptionUsecase;
import org.com.smartpayments.subscription.core.ports.in.dto.AsyncMessageInput;
import org.com.smartpayments.subscription.core.ports.in.utils.MessageUtilsPort;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.CacheDataProviderPort;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.UserDataProviderPort;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.UserSubscriptionResumeViewDataProviderPort;
import org.com.smartpayments.subscription.core.ports.out.dto.AsyncMessageOutput;
import org.com.smartpayments.subscription.core.ports.out.dto.AsyncSubscriptionPlanStateInput;
import org.com.smartpayments.subscription.core.ports.out.dto.AsyncUserSubscriptionUpdateOutput;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;

import static java.util.Objects.isNull;
import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserSubscriptionStatesConsumer {
    private final static String CACHE_KEY = "user-subscription:state:message:";
    private final static ObjectMapper mapper = new ObjectMapper();

    private final RenewUserSubscriptionUsecase renewUserSubscriptionUsecase;
    private final UserSubscriptionResumeViewDataProviderPort userSubscriptionResumeViewDataProviderPort;
    private final CacheDataProviderPort cacheDataProviderPort;
    private final UserDataProviderPort userDataProviderPort;

    private final MessageUtilsPort messageUtilsPort;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${spring.kafka.topics.user-subscription-update}")
    private String UPDATE_USER_SUBSCRIPTION_TOPIC;

    @KafkaListener(
        topics = "${spring.kafka.topics.user-subscription-states}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "topicWithDltFixedRetryContainerFactory"
    )
    public void execute(String message) {
        try {
            AsyncMessageInput<AsyncSubscriptionPlanStateInput> input = convertBody(message);

            log.info("[UserSubscriptionStatesConsumer#execute] - New message on consumer: {}", message);

            if (isNull(input) || isEmpty(input.getMessageHash()) || isNull(input.getData())) {
                log.error("[UserSubscriptionStatesConsumer#execute] - Message with invalid data will be discarded: {}", message);
                return;
            }

            final String cacheKey = CACHE_KEY + input.getMessageHash();

            if (cacheDataProviderPort.existsByKey(cacheKey)) {
                log.warn("[UserSubscriptionStatesConsumer#execute] - Message already processed: {}", message);
                return;
            }

            AsyncSubscriptionPlanStateInput inputData = input.getData();

            switch (inputData.getState()) {
                case ACTIVE_RENEWED -> {
                    renewUserSubscriptionUsecase.execute(inputData);
                }
                default -> {
                    log.warn("[UserSubscriptionStatesConsumer#execute] - Invalid message status: {}", message);
                }
            }

            sendUserSubscriptionUpdateMessage(inputData);

            cacheDataProviderPort.persist(cacheKey, "true", Duration.ofDays(5));
        } catch (Exception e) {
            log.error("[UserSubscriptionStatesConsumer#execute] - Error while consuming message: {}", message, e);
            throw e;
        }
    }

    private AsyncMessageInput<AsyncSubscriptionPlanStateInput> convertBody(String message) {
        try {
            return mapper.readValue(message,
                new TypeReference<AsyncMessageInput<AsyncSubscriptionPlanStateInput>>() {
                }
            );
        } catch (JsonProcessingException e) {
            log.error("[UserSubscriptionStatesConsumer#convertBody] - Error while converting message: {}", e.getMessage());
            return null;
        }
    }

    private void sendUserSubscriptionUpdateMessage(AsyncSubscriptionPlanStateInput input) {
        User user = userDataProviderPort.findActiveById(input.getUserId())
            .orElseThrow(UserNotFoundException::new);

        UserSubscriptionResumeView userSubscriptionResumeView = userSubscriptionResumeViewDataProviderPort
            .findByUser(user.getId()).orElseThrow(UserSubscriptionResumeViewNotFoundException::new);

        try {
            String messageIssuer = "SUBSCRIPTION";

            AsyncUserSubscriptionUpdateOutput message = AsyncUserSubscriptionUpdateOutput.builder()
                .userId(userSubscriptionResumeView.getUserId())
                .plan(EPlan.valueOf(userSubscriptionResumeView.getPlan()))
                .nextPaymentDate(userSubscriptionResumeView.getNextPaymentDate().toString())
                .recurrence(ESubscriptionRecurrence.valueOf(userSubscriptionResumeView.getRecurrence()))
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
