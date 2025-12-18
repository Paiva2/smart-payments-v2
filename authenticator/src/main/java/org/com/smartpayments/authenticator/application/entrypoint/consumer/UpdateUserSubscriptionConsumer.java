package org.com.smartpayments.authenticator.application.entrypoint.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.smartpayments.authenticator.core.domain.usecase.userSubscription.update.UserSusbcriptionUpdateUsecase;
import org.com.smartpayments.authenticator.core.ports.in.dto.AsyncMessageInput;
import org.com.smartpayments.authenticator.core.ports.in.dto.UserSubscriptionUpdateInput;
import org.com.smartpayments.authenticator.core.ports.out.dataProvider.CacheDataProviderPort;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Duration;

import static java.util.Objects.isNull;
import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateUserSubscriptionConsumer {
    private final static String CACHE_KEY = "user:user-subscription-update:message:";

    private final static ObjectMapper mapper = new ObjectMapper();

    private final CacheDataProviderPort cacheDataProviderPort;

    private final UserSusbcriptionUpdateUsecase userSusbcriptionUpdateUsecase;

    @KafkaListener(
        topics = "${spring.kafka.topics.user-subscription-update}",
        groupId = "${spring.kafka.consumer.group-id}"
    )
    public void execute(String message) {
        try {
            AsyncMessageInput<UserSubscriptionUpdateInput> input = convertBody(message);

            log.info("[UpdateUserSubscriptionConsumer#execute] - New message on consumer: {}", message);

            if (isNull(input) || isEmpty(input.getMessageHash()) || isNull(input.getData())) {
                log.error("[UpdateUserSubscriptionConsumer#execute] - Message with invalid data will be discarded: {}", message);
                return;
            }

            final String cacheKey = CACHE_KEY + input.getMessageHash();

            if (cacheDataProviderPort.existsByKey(cacheKey)) {
                log.warn("[ConfirmedPurchaseChargeConsumer#execute] - Message already processed: {}", message);
                return;
            }

            userSusbcriptionUpdateUsecase.execute(input.getData());
            
            cacheDataProviderPort.persist(cacheKey, "true", Duration.ofDays(5));
        } catch (Exception e) {
            log.error("[UpdateUserSubscriptionConsumer#execute] - Error while consuming message. Message: {} | Error: {}", message, e.getMessage());
            throw e;
        }
    }

    private AsyncMessageInput<UserSubscriptionUpdateInput> convertBody(String message) {
        try {
            return mapper.readValue(message,
                new TypeReference<AsyncMessageInput<UserSubscriptionUpdateInput>>() {
                }
            );
        } catch (JsonProcessingException e) {
            log.error("[UpdateUserSubscriptionConsumer#convertBody] - Error while converting message: {}", e.getMessage());
            return null;
        }
    }
}
