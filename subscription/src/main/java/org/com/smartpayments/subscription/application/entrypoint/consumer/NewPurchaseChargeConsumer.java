package org.com.smartpayments.subscription.application.entrypoint.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.smartpayments.subscription.core.domain.usecase.purchaseCharge.createPurchaseCharge.CreatePurchaseChargeUsecase;
import org.com.smartpayments.subscription.core.ports.in.dto.AsyncMessageInput;
import org.com.smartpayments.subscription.core.ports.in.dto.CreatePurchaseChargeInput;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.CacheDataProviderPort;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Duration;

import static java.util.Objects.isNull;
import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j
@Component
@AllArgsConstructor
public class NewPurchaseChargeConsumer {
    private final static String CACHE_KEY = "purchase:new-charge:message:";
    private final static ObjectMapper mapper = new ObjectMapper();

    private final CreatePurchaseChargeUsecase createPurchaseChargeUsecase;

    private final CacheDataProviderPort cacheDataProviderPort;

    @KafkaListener(
        topics = "${spring.kafka.topics.new-purchase-charge}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "topicWithDltFixedRetryContainerFactory"
    )
    public void execute(String message) {
        try {
            AsyncMessageInput<CreatePurchaseChargeInput> input = convertBody(message);

            log.info("[NewPurchaseChargeConsumer#execute] - New message on consumer: {}", message);

            if (isNull(input) || isEmpty(input.getMessageHash()) || isNull(input.getData())) {
                log.error("[NewPurchaseChargeConsumer#execute] - Message with invalid data will be discarded: {}", message);
                return;
            }

            final String cacheKey = CACHE_KEY + input.getMessageHash();

            if (cacheDataProviderPort.existsByKey(cacheKey)) {
                log.warn("[NewPurchaseChargeConsumer#execute] - Message already processed: {}", message);
                return;
            }

            CreatePurchaseChargeInput newPurchaseChargeInput = input.getData();
            createPurchaseChargeUsecase.execute(newPurchaseChargeInput);

            cacheDataProviderPort.persist(cacheKey, "true", Duration.ofDays(5));
        } catch (Exception e) {
            log.error("[NewPurchaseChargeConsumer#execute] - Error while consuming message: {}", message, e);
            throw e;
        }
    }

    private AsyncMessageInput<CreatePurchaseChargeInput> convertBody(String message) {
        try {
            return mapper.readValue(message,
                new TypeReference<AsyncMessageInput<CreatePurchaseChargeInput>>() {
                }
            );
        } catch (JsonProcessingException e) {
            log.error("[NewPurchaseChargeConsumer#convertBody] - Error while converting message: {}", e.getMessage());
            return null;
        }
    }
}
