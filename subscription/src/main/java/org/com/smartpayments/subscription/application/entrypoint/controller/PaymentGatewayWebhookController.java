package org.com.smartpayments.subscription.application.entrypoint.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.smartpayments.subscription.core.common.exception.base.ForbiddenException;
import org.com.smartpayments.subscription.core.domain.enums.EPaymentGatewayEvent;
import org.com.smartpayments.subscription.core.domain.enums.EPurchaseChargeStatus;
import org.com.smartpayments.subscription.core.ports.in.dto.*;
import org.com.smartpayments.subscription.core.ports.in.utils.MessageUtilsPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

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

    @Value("${spring.kafka.topics.payment-events}")
    private String paymentEventsTopic;

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
        AsyncMessageInput<Object> message = createAsyncMessage();
        PaymentEventInput<Object> paymentEvent = PaymentEventInput.builder()
            .event(event)
            .customerId(input.getPayment().getCustomer())
            .build();

        String messageKey = input.isFromSubscription() ? input.getPayment().getSubscription() : input.getPayment().getId();

        switch (event) {
            case PAYMENT_CREATED -> {
                CreatePurchaseChargeInput createPurchaseChargeInput = mountNewPurchaseChargeInput(input, event);
                paymentEvent.setData(createPurchaseChargeInput);
            }
            case PAYMENT_RECEIVED,
                 PAYMENT_CONFIRMED -> {
                PurchaseChargeConfirmedInput purchaseChargeConfirmedInput = mountPurchaseChargeConfirmedInput(input, event);
                paymentEvent.setData(purchaseChargeConfirmedInput);
            }
            case PAYMENT_OVERDUE -> {
                PurchaseChargeOverdueInput purchaseChargeOverdueInput = mountPurchaseChargeOverdueInput(input, event);
                paymentEvent.setData(purchaseChargeOverdueInput);
            }
            case PAYMENT_REFUNDED -> {
                RefundedPurchaseChargeInput purchaseChargeRefundedInput = mountPurchaseChargeRefundedInput(input, event);
                paymentEvent.setData(purchaseChargeRefundedInput);
            }
            default ->
                log.warn("[PaymentGatewayWebhookController#handle] - Received an request with unmapped event: {}, message will be discarded!", input.getEvent());
        }

        message.setData(paymentEvent);
        sendMessage(paymentEventsTopic, messageKey, message);
    }

    private AsyncMessageInput<Object> createAsyncMessage() {
        final String issuer = "SUBSCRIPTION";
        return new AsyncMessageInput<>(messageUtilsPort.generateMessageHash(issuer), new Date(), issuer, null);
    }

    private void sendMessage(String topic, String key, Object data) throws JsonProcessingException {
        kafkaTemplate.send(topic, key, mapper.writeValueAsString(data));
    }

    private CreatePurchaseChargeInput mountNewPurchaseChargeInput(PaymentGatewayWebhookInput input, EPaymentGatewayEvent event) {
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
            .event(event)
            .build();
    }

    private PurchaseChargeConfirmedInput mountPurchaseChargeConfirmedInput(PaymentGatewayWebhookInput input, EPaymentGatewayEvent event) {
        return PurchaseChargeConfirmedInput.builder()
            .customerId(input.getPayment().getCustomer())
            .isFromSubscription(input.isFromSubscription())
            .externalPurchaseId(input.isFromSubscription() ? input.getPayment().getSubscription() : input.getPayment().getId())
            .externalChargeId(input.getPayment().getId())
            .paymentDate(parseDate(input.getPayment().getClientPaymentDate()))
            .dueDate(handleDueDate(input.getPayment().getDueDate()))
            .event(event)
            .build();
    }

    private PurchaseChargeOverdueInput mountPurchaseChargeOverdueInput(PaymentGatewayWebhookInput input, EPaymentGatewayEvent event) {
        return PurchaseChargeOverdueInput.builder()
            .customerId(input.getPayment().getCustomer())
            .isFromSubscription(input.isFromSubscription())
            .externalPurchaseId(input.isFromSubscription() ? input.getPayment().getSubscription() : input.getPayment().getId())
            .externalChargeId(input.getPayment().getId())
            .dueDate(handleDueDate(input.getPayment().getDueDate()))
            .event(event)
            .build();
    }

    private RefundedPurchaseChargeInput mountPurchaseChargeRefundedInput(PaymentGatewayWebhookInput input, EPaymentGatewayEvent event) {
        return RefundedPurchaseChargeInput.builder()
            .customerId(input.getPayment().getCustomer())
            .isFromSubscription(input.isFromSubscription())
            .externalPurchaseId(input.isFromSubscription() ? input.getPayment().getSubscription() : input.getPayment().getId())
            .externalChargeId(input.getPayment().getId())
            .event(event)
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
