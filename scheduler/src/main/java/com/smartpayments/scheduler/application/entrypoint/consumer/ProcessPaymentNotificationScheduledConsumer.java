package com.smartpayments.scheduler.application.entrypoint.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartpayments.scheduler.core.common.exception.SubscriptionClientErrorException;
import com.smartpayments.scheduler.core.domain.usecase.paymentScheduledNotification.processPaymentScheduledNotification.ProcessPaymentScheduledNotificationUsecase;
import com.smartpayments.scheduler.core.ports.in.external.messaging.AsyncMessageInput;
import com.smartpayments.scheduler.core.ports.in.usecase.dto.ProcessPaymentScheduledNotificationInput;
import com.smartpayments.scheduler.core.ports.out.dataProvider.CacheDataProviderPort;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Duration;

import static java.util.Objects.isNull;

@Slf4j
@Component
@AllArgsConstructor
public class ProcessPaymentNotificationScheduledConsumer {
    private final static String CACHE_KEY = "payment-notification:process:";
    private final static ObjectMapper mapper = new ObjectMapper();

    private final ProcessPaymentScheduledNotificationUsecase processPaymentScheduledNotificationUsecase;

    private final CacheDataProviderPort cacheDataProviderPort;

    @KafkaListener(
        topics = "${spring.kafka.topics.process-scheduled-payment-notifications}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "topicWithDltFixedRetryContainerFactory"
    )
    public void execute(String message) {
        try {
            AsyncMessageInput<ProcessPaymentScheduledNotificationInput> input = convertBody(message);

            log.info("[ProcessPaymentNotificationScheduledConsumer#execute] new message on consumer: {}", message);

            if (isNull(input) || isNull(input.getData())) {
                log.error("[ProcessPaymentNotificationScheduledConsumer#execute] message without data will be discarded: {}", message);
                return;
            }

            final String cacheKey = CACHE_KEY + input.getMessageHash();

            if (cacheDataProviderPort.existsByKey(cacheKey)) {
                log.warn("[ProcessPaymentNotificationScheduledConsumer#execute] - Message already processed: {}", message);
                return;
            }

            ProcessPaymentScheduledNotificationInput processInput = input.getData();
            processInput.setMessageHash(input.getMessageHash());
            processPaymentScheduledNotificationUsecase.execute(processInput);

            cacheDataProviderPort.persist(cacheKey, "true", Duration.ofDays(5));
        } catch (SubscriptionClientErrorException e) {
            log.error("[ProcessPaymentNotificationScheduledConsumer#execute] - Client Error while consuming message: {}", message, e);
        } catch (Exception e) {
            log.error("[ProcessPaymentNotificationScheduledConsumer#execute] - Error while consuming message: {}", message, e);
            throw e;
        }
    }

    private AsyncMessageInput<ProcessPaymentScheduledNotificationInput> convertBody(String message) {
        try {
            return mapper.readValue(message,
                new TypeReference<AsyncMessageInput<ProcessPaymentScheduledNotificationInput>>() {
                }
            );
        } catch (JsonProcessingException e) {
            log.error("[ProcessPaymentNotificationScheduledConsumer#convertBody] - Error while converting message: {}", e.getMessage());
            return null;
        }
    }
}
