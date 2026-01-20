package org.com.smartpayments.subscription.application.entrypoint.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.smartpayments.subscription.core.common.exception.UserNotFoundException;
import org.com.smartpayments.subscription.core.domain.model.User;
import org.com.smartpayments.subscription.core.domain.usecase.userSubscription.renewUserSubscription.RenewUserSubscriptionUsecase;
import org.com.smartpayments.subscription.core.domain.usecase.userSubscription.revokeUserSubscription.RevokeUserSubscriptionUsecase;
import org.com.smartpayments.subscription.core.domain.usecase.userSubscription.sendUserSubscriptionUpdateMessage.SendUserSubscriptionUpdateMessageUsecase;
import org.com.smartpayments.subscription.core.ports.in.dto.AsyncMessageInput;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.CacheDataProviderPort;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.UserDataProviderPort;
import org.com.smartpayments.subscription.core.ports.out.dto.AsyncSubscriptionPlanStateInput;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Duration;

import static java.util.Objects.isNull;
import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j
@Service
@AllArgsConstructor
public class UserSubscriptionStatesConsumer {
    private final static String CACHE_KEY = "user-subscription:state:message:";
    private final static ObjectMapper mapper = new ObjectMapper();

    private final RenewUserSubscriptionUsecase renewUserSubscriptionUsecase;
    private final RevokeUserSubscriptionUsecase revokeUserSubscriptionUsecase;
    private final SendUserSubscriptionUpdateMessageUsecase sendUserSubscriptionUpdateMessageUsecase;

    private final CacheDataProviderPort cacheDataProviderPort;
    private final UserDataProviderPort userDataProviderPort;

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
                case EXPIRED, CANCELLED -> {
                    revokeUserSubscriptionUsecase.execute(inputData);
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

        sendUserSubscriptionUpdateMessageUsecase.execute(user);
    }
}
