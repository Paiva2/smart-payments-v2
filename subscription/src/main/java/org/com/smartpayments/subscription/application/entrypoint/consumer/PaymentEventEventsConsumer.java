package org.com.smartpayments.subscription.application.entrypoint.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.smartpayments.subscription.core.common.exception.UserNotFoundException;
import org.com.smartpayments.subscription.core.domain.model.User;
import org.com.smartpayments.subscription.core.domain.usecase.purchaseCharge.confirmPurchaseCharge.ConfirmPurchaseChargeUsecase;
import org.com.smartpayments.subscription.core.domain.usecase.purchaseCharge.createPurchaseCharge.CreatePurchaseChargeUsecase;
import org.com.smartpayments.subscription.core.domain.usecase.purchaseCharge.overduePurchaseCharge.OverduePurchaseChargeUsecase;
import org.com.smartpayments.subscription.core.domain.usecase.purchaseCharge.refundedPurchaseCharge.RefundedPurchaseChargeUsecase;
import org.com.smartpayments.subscription.core.domain.usecase.userSubscription.sendUserSubscriptionUpdateMessage.SendUserSubscriptionUpdateMessageUsecase;
import org.com.smartpayments.subscription.core.ports.in.dto.*;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.CacheDataProviderPort;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.UserDataProviderPort;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Duration;

import static java.util.Objects.isNull;
import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentEventEventsConsumer {
    private final static String CACHE_KEY = "purchase-charge:message-event:";
    private final static ObjectMapper mapper = new ObjectMapper();

    private final CreatePurchaseChargeUsecase createPurchaseChargeUsecase;
    private final OverduePurchaseChargeUsecase overduePurchaseChargeUsecase;
    private final ConfirmPurchaseChargeUsecase confirmPurchaseChargeUsecase;
    private final RefundedPurchaseChargeUsecase refundedPurchaseChargeUsecase;

    private final UserDataProviderPort userDataProviderPort;
    private final CacheDataProviderPort cacheDataProviderPort;

    private final SendUserSubscriptionUpdateMessageUsecase sendUserSubscriptionUpdateMessageUsecase;

    @KafkaListener(
        topics = "${spring.kafka.topics.payment-events}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "topicWithDltFixedRetryContainerFactory"
    )
    public void execute(String message) {
        try {
            AsyncMessageInput<PaymentEventInput> messageInput = convertMessageBody(message);

            log.info("[PaymentEventEventsConsumer#execute] - New message on consumer: {}", message);

            if (isNull(messageInput) || isEmpty(messageInput.getMessageHash()) || isNull(messageInput.getData())) {
                log.error("[PaymentEventEventsConsumer#execute] - Message with invalid data will be discarded: {}", message);
                return;
            }

            final String cacheKey = CACHE_KEY + messageInput.getMessageHash();

            if (cacheDataProviderPort.existsByKey(cacheKey)) {
                log.warn("[PaymentEventEventsConsumer#execute] - Message already processed: {}", message);
                return;
            }

            Boolean shouldSendUpdate = false;
            String customerId = messageInput.getData().getCustomerId();
            PaymentEventInput<Object> paymentEvent = messageInput.getData();

            switch (messageInput.getData().getEvent()) {
                case PAYMENT_CREATED -> {
                    CreatePurchaseChargeInput inputData = (CreatePurchaseChargeInput) convertPaymentGatewayEventData(paymentEvent.getData(), CreatePurchaseChargeInput.class);
                    createPurchaseChargeUsecase.execute(inputData);
                }
                case PAYMENT_CONFIRMED,
                     PAYMENT_RECEIVED -> {
                    PurchaseChargeConfirmedInput inputData = (PurchaseChargeConfirmedInput) convertPaymentGatewayEventData(paymentEvent.getData(), PurchaseChargeConfirmedInput.class);
                    confirmPurchaseChargeUsecase.execute(inputData);
                    shouldSendUpdate = true;
                }
                case PAYMENT_OVERDUE -> {
                    PurchaseChargeOverdueInput inputData = (PurchaseChargeOverdueInput) convertPaymentGatewayEventData(paymentEvent.getData(), PurchaseChargeOverdueInput.class);
                    shouldSendUpdate = overduePurchaseChargeUsecase.execute(inputData);
                }
                case PAYMENT_REFUNDED -> {
                    RefundedPurchaseChargeInput inputData = (RefundedPurchaseChargeInput) convertPaymentGatewayEventData(paymentEvent.getData(), RefundedPurchaseChargeInput.class);
                    refundedPurchaseChargeUsecase.execute(inputData);
                    shouldSendUpdate = true;
                }
            }

            if (shouldSendUpdate) {
                sendUserSubscriptionUpdateMessage(customerId);
            }

            cacheDataProviderPort.persist(cacheKey, "true", Duration.ofDays(5));
        } catch (Exception e) {
            log.error("[PaymentEventEventsConsumer#execute] - Error while consuming message: {}", message, e);
            throw e;
        }
    }

    private AsyncMessageInput<PaymentEventInput> convertMessageBody(String message) {
        try {
            return mapper.readValue(message,
                new TypeReference<AsyncMessageInput<PaymentEventInput>>() {
                }
            );
        } catch (JsonProcessingException e) {
            log.error("[PaymentEventEventsConsumer#convertMessageBody] - Error while converting message: {}", e.getMessage());
            return null;
        }
    }

    private Object convertPaymentGatewayEventData(Object message, Class inputClass) {
        return mapper.convertValue(message, inputClass);
    }

    private void sendUserSubscriptionUpdateMessage(String customerId) {
        User user = userDataProviderPort.findByPaymentGatewayId(customerId)
            .orElseThrow(UserNotFoundException::new);

        sendUserSubscriptionUpdateMessageUsecase.execute(user);
    }
}
