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
import org.com.smartpayments.subscription.core.domain.enums.ESubscriptionStatus;
import org.com.smartpayments.subscription.core.domain.model.User;
import org.com.smartpayments.subscription.core.domain.model.views.UserSubscriptionResumeView;
import org.com.smartpayments.subscription.core.domain.usecase.purchaseCharge.confirmPurchaseCharge.ConfirmPurchaseChargeUsecase;
import org.com.smartpayments.subscription.core.domain.usecase.purchaseCharge.createPurchaseCharge.CreatePurchaseChargeUsecase;
import org.com.smartpayments.subscription.core.domain.usecase.purchaseCharge.overduePurchaseCharge.OverduePurchaseChargeUsecase;
import org.com.smartpayments.subscription.core.domain.usecase.purchaseCharge.refundedPurchaseCharge.RefundedPurchaseChargeUsecase;
import org.com.smartpayments.subscription.core.ports.in.dto.*;
import org.com.smartpayments.subscription.core.ports.in.utils.MessageUtilsPort;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.CacheDataProviderPort;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.UserDataProviderPort;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.UserSubscriptionResumeViewDataProviderPort;
import org.com.smartpayments.subscription.core.ports.out.dto.AsyncMessageOutput;
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
public class PaymentEventEventsConsumer {
    private final static String CACHE_KEY = "purchase-charge:message-event:";
    private final static ObjectMapper mapper = new ObjectMapper();

    private final CreatePurchaseChargeUsecase createPurchaseChargeUsecase;
    private final OverduePurchaseChargeUsecase overduePurchaseChargeUsecase;
    private final ConfirmPurchaseChargeUsecase confirmPurchaseChargeUsecase;
    private final RefundedPurchaseChargeUsecase refundedPurchaseChargeUsecase;

    private final UserSubscriptionResumeViewDataProviderPort userSubscriptionResumeViewDataProviderPort;
    private final CacheDataProviderPort cacheDataProviderPort;
    private final UserDataProviderPort userDataProviderPort;

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final MessageUtilsPort messageUtilsPort;

    @Value("${spring.kafka.topics.user-subscription-update}")
    private String UPDATE_USER_SUBSCRIPTION_TOPIC;

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

        UserSubscriptionResumeView userSubscriptionResumeView = userSubscriptionResumeViewDataProviderPort
            .findByUser(user.getId()).orElseThrow(UserSubscriptionResumeViewNotFoundException::new);

        try {
            String messageIssuer = "SUBSCRIPTION";

            AsyncUserSubscriptionUpdateOutput message = AsyncUserSubscriptionUpdateOutput.builder()
                .userId(userSubscriptionResumeView.getUserId())
                .plan(EPlan.valueOf(userSubscriptionResumeView.getPlan()))
                .status(ESubscriptionStatus.valueOf(userSubscriptionResumeView.getStatus()))
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
