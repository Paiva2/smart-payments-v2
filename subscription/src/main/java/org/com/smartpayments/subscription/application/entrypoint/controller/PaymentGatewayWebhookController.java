package org.com.smartpayments.subscription.application.entrypoint.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.smartpayments.subscription.core.common.exception.base.ForbiddenException;
import org.com.smartpayments.subscription.core.domain.enums.EPaymentGatewayEvent;
import org.com.smartpayments.subscription.core.domain.enums.EPurchaseChargeStatus;
import org.com.smartpayments.subscription.core.ports.in.dto.AsyncMessageInput;
import org.com.smartpayments.subscription.core.ports.in.dto.CreatePurchaseChargeInput;
import org.com.smartpayments.subscription.core.ports.in.dto.PaymentGatewayWebhookInput;
import org.com.smartpayments.subscription.core.ports.in.dto.PurchaseChargeConfirmedInput;
import org.com.smartpayments.subscription.core.ports.in.utils.MessageUtilsPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import static java.util.Objects.isNull;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("${server.api-suffix}")
public class PaymentGatewayWebhookController {
    private static final ObjectMapper mapper = new ObjectMapper();

    private final MessageUtilsPort messageUtilsPort;

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${external.payment-gateway.webhook.api-key}")
    private String webhookKey;

    @Value("${spring.kafka.topics.new-purchase-charge}")
    private String purchaseChargeCreatedTopic;

    @Value("${spring.kafka.topics.confirmed-purchase-charge}")
    private String purchaseChargeConfirmedTopic;

    @PostMapping("purchase/webhook")
    public ResponseEntity<Void> handle(
        @RequestHeader(value = "asaas-access-token", required = false) String apiKey,
        @RequestBody PaymentGatewayWebhookInput input
    ) {
        if (!Objects.equals(webhookKey, apiKey)) {
            log.error("[PaymentGatewayWebhookController#handle] - Received an request with wrong api key!");
            throw new ForbiddenException("Invalid webhook key!");
        }

        log.info("[PaymentGatewayWebhookController#handle] - New webhook message received: {}", input);

        EPaymentGatewayEvent eventReceived = parseEventMessage(input.getEvent());

        if (isNull(eventReceived)) {
            log.warn("[PaymentGatewayWebhookController#handle] - Received an request with unmapped event: {}, message will be discarded!", input.getEvent());
            return ResponseEntity.noContent().build();
        }

        try {
            sendEventToQueue(input, eventReceived);
        } catch (Exception e) {
            String message = "Error while sending event to queue!";
            log.error(message, e);
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.noContent().build();
    }

    private EPaymentGatewayEvent parseEventMessage(String event) {
        try {
            return EPaymentGatewayEvent.valueOf(event);
        } catch (Exception e) {
            return null;
        }
    }

    private void sendEventToQueue(PaymentGatewayWebhookInput input, EPaymentGatewayEvent event) throws JsonProcessingException {
        AsyncMessageInput<Object> messageInput = createAsyncMessage();
        String messageKey = input.isFromSubscription() ? input.getPayment().getSubscription() : input.getPayment().getId();

        switch (event) {
            case PAYMENT_CREATED -> {
                CreatePurchaseChargeInput createPurchaseChargeInput = mountNewPurchaseChargeInput(input);
                messageInput.setData(createPurchaseChargeInput);
                sendMessage(purchaseChargeCreatedTopic, messageKey, messageInput);
            }
            case PAYMENT_RECEIVED,
                 PAYMENT_CONFIRMED -> {
                PurchaseChargeConfirmedInput purchaseChargeConfirmedInput = mountPurchaseChargeConfirmedInput(input);
                messageInput.setData(purchaseChargeConfirmedInput);
                sendMessage(purchaseChargeConfirmedTopic, messageKey, messageInput);
            }
            default ->
                log.warn("[PaymentGatewayWebhookController#handle] - Received an request with unmapped event: {}, message will be discarded!", input.getEvent());
        }
    }

    private AsyncMessageInput<Object> createAsyncMessage() {
        final String issuer = "SUBSCRIPTION";
        return new AsyncMessageInput<>(messageUtilsPort.generateMessageHash(issuer), new Date(), issuer, null);
    }

    private void sendMessage(String topic, String key, Object data) throws JsonProcessingException {
        kafkaTemplate.send(topic, key, mapper.writeValueAsString(data));
    }

    private CreatePurchaseChargeInput mountNewPurchaseChargeInput(PaymentGatewayWebhookInput input) {
        return CreatePurchaseChargeInput.builder()
            .externalPurchaseId(input.isFromSubscription() ? input.getPayment().getSubscription() : input.getPayment().getId())
            .externalChargeId(input.getPayment().getId())
            .value(input.getPayment().getValue())
            .description(input.getPayment().getDescription())
            .billingType(input.getPayment().getBillingType())
            .pixTransaction(input.getPayment().getPixTransaction())
            .status(EPurchaseChargeStatus.PENDING)
            .dueDate(handleDueDate(input.getPayment().getDueDate()))
            .invoiceUrl(input.getPayment().getInvoiceUrl())
            .bankSlipUrl(input.getPayment().getBankSlipUrl())
            .build();
    }

    private PurchaseChargeConfirmedInput mountPurchaseChargeConfirmedInput(PaymentGatewayWebhookInput input) {
        return PurchaseChargeConfirmedInput.builder()
            .customerId(input.getPayment().getCustomer())
            .isFromSubscription(input.isFromSubscription())
            .externalPurchaseId(input.isFromSubscription() ? input.getPayment().getSubscription() : input.getPayment().getId())
            .externalChargeId(input.getPayment().getId())
            .paymentDate(parseDate(input.getPayment().getClientPaymentDate()))
            .dueDate(handleDueDate(input.getPayment().getDueDate()))
            .build();
    }

    private Date parseDate(String date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            return sdf.parse(date);
        } catch (ParseException e) {
            throw new RuntimeException("Invalid date format: " + date, e);
        }
    }

    private Date handleDueDate(String dueDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(parseDate(dueDate));
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 59);
        return calendar.getTime();
    }
}
