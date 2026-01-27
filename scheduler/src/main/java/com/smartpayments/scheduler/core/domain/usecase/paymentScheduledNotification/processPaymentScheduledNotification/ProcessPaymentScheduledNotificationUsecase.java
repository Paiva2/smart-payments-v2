package com.smartpayments.scheduler.core.domain.usecase.paymentScheduledNotification.processPaymentScheduledNotification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.messaging_gateway.avro.CustomMessage;
import com.smartpayments.scheduler.core.common.enums.EChannelTypeMessageGateway;
import com.smartpayments.scheduler.core.common.exception.PaymentScheduledNotificationNotFoundException;
import com.smartpayments.scheduler.core.domain.enums.ENotificationExecutionStatus;
import com.smartpayments.scheduler.core.domain.model.PaymentReceiver;
import com.smartpayments.scheduler.core.domain.model.PaymentScheduledNotification;
import com.smartpayments.scheduler.core.domain.usecase.paymentScheduledNotification.processPaymentScheduledNotification.exception.ProcessPaymentScheduledNotificationException;
import com.smartpayments.scheduler.core.ports.in.external.messaging.UserSubscriptionCreditInput;
import com.smartpayments.scheduler.core.ports.in.usecase.UsecaseVoidPort;
import com.smartpayments.scheduler.core.ports.in.usecase.dto.ProcessPaymentScheduledNotificationInput;
import com.smartpayments.scheduler.core.ports.out.dataProvider.PaymentScheduledNotificationDataProviderPort;
import com.smartpayments.scheduler.core.ports.out.external.dto.ConsumeUserSubscriptionCreditsOutput;
import com.smartpayments.scheduler.core.ports.out.external.dto.UserSubscriptionOutput;
import com.smartpayments.scheduler.core.ports.out.external.subscription.SubscriptionClientPort;
import com.smartpayments.scheduler.core.ports.out.utils.MessageUtilsPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessPaymentScheduledNotificationUsecase implements UsecaseVoidPort<ProcessPaymentScheduledNotificationInput> {
    private final static String ISSUER = "SCHEDULER";
    private final static String EMAIL_TEMPLATE = "notify-payment-scheduled-notification";
    private final static String SMS_TEMPLATE = "notify-payment-scheduled-notification";
    private final static String WHATSAPP_TEMPLATE = "notify-payment-scheduled-notification";
    private final static String NO_CREDITS_AVAILABLE_EMAIL_TEMPLATE = "no-credits-available";
    private final static String EXTERNAL_CREDIT_CONSUMPTION_SUCCESS = "SUCCESS";
    private final static String EXTERNAL_CREDIT_CONSUMPTION_NO_BALANCE = "NO_BALANCE_AVAILABLE";
    private final static String CREDIT_USAGE_REASON = "PAYMENT_SCHEDULED_NOTIFICATION";

    private final static ObjectMapper mapper = new ObjectMapper();

    private final PaymentScheduledNotificationDataProviderPort paymentScheduledNotificationDataProviderPort;

    private final SubscriptionClientPort subscriptionClientPort;

    private final MessageUtilsPort messageUtilsPort;
    private final KafkaTemplate<String, Object> kafkaAvroTemplate;

    @Value("${spring.kafka.topics.messaging-gateway}")
    private String messageGatewayTopic;

    @Override
    @Transactional
    public void execute(ProcessPaymentScheduledNotificationInput input) {
        LinkedHashSet<com.messaging_gateway.avro.CustomMessage> asyncMessages = new LinkedHashSet<>();
        PaymentScheduledNotification paymentScheduledNotification = findNotification(input.getId());
        String notificationTitle = "Smart Payments - " + paymentScheduledNotification.getTitle();
        List<EChannelTypeMessageGateway> channelsWithoutCreditToBeNotified = new ArrayList<>();

        try {
            UserSubscriptionOutput userSubscriptionOutput = findUserSubscription(paymentScheduledNotification.getUserId());

            UserSubscriptionCreditInput userSubscriptionCreditInput = UserSubscriptionCreditInput.builder()
                .usageReason(CREDIT_USAGE_REASON)
                .usageReasonId(paymentScheduledNotification.getId().toString())
                .userId(paymentScheduledNotification.getUserId())
                .emailCredits(paymentScheduledNotification.getNotifyEmail() ? 1 : 0)
                .smsCredits(paymentScheduledNotification.getNotifySms() ? 1 : 0)
                .whatsAppCredits(paymentScheduledNotification.getNotifyWhatsApp() ? 1 : 0)
                .build();

            ConsumeUserSubscriptionCreditsOutput consumptionResult = handleConsumeCreditsExternal(input.getMessageHash(), userSubscriptionCreditInput);

            if (consumptionResult.getEmail().equals(EXTERNAL_CREDIT_CONSUMPTION_SUCCESS)) {
                fillAsyncPaymentNotificationToBeSent(
                    EChannelTypeMessageGateway.EMAIL,
                    notificationTitle,
                    userSubscriptionOutput.getEmail(),
                    null,
                    EMAIL_TEMPLATE,
                    mountPaymentNotificationEmailVariables(userSubscriptionOutput, paymentScheduledNotification),
                    asyncMessages
                );
            } else if (consumptionResult.getEmail().equals(EXTERNAL_CREDIT_CONSUMPTION_NO_BALANCE)) {
                channelsWithoutCreditToBeNotified.add(EChannelTypeMessageGateway.EMAIL);
            }

            if (consumptionResult.getSms().equals(EXTERNAL_CREDIT_CONSUMPTION_SUCCESS)) {
                fillAsyncPaymentNotificationToBeSent(
                    EChannelTypeMessageGateway.SMS,
                    notificationTitle,
                    userSubscriptionOutput.getPhoneNumber(),
                    null,
                    SMS_TEMPLATE,
                    mountPaymentNotificationSmsVariables(userSubscriptionOutput, paymentScheduledNotification),
                    asyncMessages
                );
            } else if (consumptionResult.getSms().equals(EXTERNAL_CREDIT_CONSUMPTION_NO_BALANCE)) {
                channelsWithoutCreditToBeNotified.add(EChannelTypeMessageGateway.SMS);
            }

            if (consumptionResult.getWhatsapp().equals(EXTERNAL_CREDIT_CONSUMPTION_SUCCESS)) {
                fillAsyncPaymentNotificationToBeSent(
                    EChannelTypeMessageGateway.WHATS_APP,
                    notificationTitle,
                    userSubscriptionOutput.getPhoneNumber(),
                    null,
                    WHATSAPP_TEMPLATE,
                    mountPaymentNotificationWhatsAppVariables(userSubscriptionOutput, paymentScheduledNotification),
                    asyncMessages
                );
            } else if (consumptionResult.getWhatsapp().equals(EXTERNAL_CREDIT_CONSUMPTION_NO_BALANCE)) {
                channelsWithoutCreditToBeNotified.add(EChannelTypeMessageGateway.WHATS_APP);
            }

            fillNoCreditMessages(asyncMessages, channelsWithoutCreditToBeNotified, userSubscriptionOutput.getEmail());

            paymentScheduledNotification.setLastExecutionStatus(ENotificationExecutionStatus.SUCCESS);
            persistNotification(paymentScheduledNotification);

            sendAsyncMessageGatewayMessages(asyncMessages);
        } catch (Exception e) {
            log.error("[ProcessPaymentScheduledNotificationUsecase#execute] - Error while processing payment scheduled notification! Id: {}", input.getId(), e);
            paymentScheduledNotification.setLastExecutionStatus(ENotificationExecutionStatus.FAIL);
            persistNotification(paymentScheduledNotification);
            throw new ProcessPaymentScheduledNotificationException("Error while processing payment scheduled notification!");
        }
    }

    private com.messaging_gateway.avro.CustomMessage mountCustomMessageGatewayInput(EChannelTypeMessageGateway channelType, String template, String message, String subject, String to, Map<CharSequence, Object> variables) {
        return com.messaging_gateway.avro.CustomMessage.newBuilder()
            .setChannelType(channelType.name())
            .setSubject(subject)
            .setTemplate(template)
            .setTo(to)
            .setVariables(variables)
            .setMessage(message)
            .build();
    }

    private HashMap<CharSequence, Object> mountNoCreditsEmailVariables(List<EChannelTypeMessageGateway> channelTypes) {
        return new HashMap<>() {{
            put("${PAYMENT_NOTIFICATION_TYPE}", String.join(", ", channelTypes.stream().map(EChannelTypeMessageGateway::getType).toList()));
        }};
    }

    private HashMap<CharSequence, Object> mountPaymentNotificationWhatsAppVariables(UserSubscriptionOutput userSubscriptionOutput, PaymentScheduledNotification paymentScheduledNotification) {
        return new HashMap<>() {{
            put("${USER_NAME}", userSubscriptionOutput.getFirstName());
            put("${PAYMENT_NOTIFICATION_TITLE}", paymentScheduledNotification.getTitle());
            put("${PAYMENT_NOTIFICATION_DESCRIPTION}", paymentScheduledNotification.getDescription());
            put("${PAYMENT_NOTIFICATION_VALUE}", paymentScheduledNotification.getValue());
            put("${PAYMENT_NOTIFICATION_RECEIVERS_LIST}", String.join(", ", paymentScheduledNotification.getReceivers().stream().map(PaymentReceiver::getIdentification).toList()));
        }};
    }

    private HashMap<CharSequence, Object> mountPaymentNotificationSmsVariables(UserSubscriptionOutput userSubscriptionOutput, PaymentScheduledNotification paymentScheduledNotification) {
        return new HashMap<>() {{
            put("${USER_NAME}", userSubscriptionOutput.getFirstName());
            put("${PAYMENT_NOTIFICATION_TITLE}", paymentScheduledNotification.getTitle());
            put("${PAYMENT_NOTIFICATION_VALUE}", paymentScheduledNotification.getValue());
        }};
    }

    private String fillReceiverListForEmail(List<PaymentReceiver> receivers) {
        return receivers.stream().collect(
            StringBuilder::new,
            (sb, item) -> sb.append(String.format("<li>%s</li>\n", item.getIdentification())),
            StringBuilder::append
        ).toString();
    }

    private HashMap<CharSequence, Object> mountPaymentNotificationEmailVariables(UserSubscriptionOutput userSubscriptionOutput, PaymentScheduledNotification paymentScheduledNotification) {
        return new HashMap<>() {{
            put("${USER_NAME}", userSubscriptionOutput.getFirstName());
            put("${PAYMENT_NOTIFICATION_TITLE}", paymentScheduledNotification.getTitle());
            put("${PAYMENT_NOTIFICATION_DESCRIPTION}", paymentScheduledNotification.getDescription());
            put("${PAYMENT_NOTIFICATION_VALUE}", paymentScheduledNotification.getValue());
            put("${PAYMENT_NOTIFICATION_NEXT_NOTIFICATION_DATE}", paymentScheduledNotification.getNextDate().toString());
            put("${PAYMENT_NOTIFICATION_RECEIVERS_LIST}", fillReceiverListForEmail(paymentScheduledNotification.getReceivers()));
        }};
    }

    public void persistNotification(PaymentScheduledNotification paymentScheduledNotification) {
        paymentScheduledNotificationDataProviderPort.persist(paymentScheduledNotification);
    }

    private void fillAsyncPaymentNotificationToBeSent(
        EChannelTypeMessageGateway channel,
        String title,
        String to,
        String message,
        String template,
        HashMap<CharSequence, Object> variables,
        LinkedHashSet<com.messaging_gateway.avro.CustomMessage> asyncMessages
    ) {
        com.messaging_gateway.avro.CustomMessage customMessageInput = mountCustomMessageGatewayInput(
            channel,
            template,
            message,
            title,
            to,
            variables
        );

        asyncMessages.add(customMessageInput);
    }

    private ConsumeUserSubscriptionCreditsOutput handleConsumeCreditsExternal(String messageHash, UserSubscriptionCreditInput input) {
        return subscriptionClientPort.consumeUserSubscriptionCredits(messageHash, input);
    }

    private void fillNoCreditMessages(LinkedHashSet<com.messaging_gateway.avro.CustomMessage> asyncMessages, List<EChannelTypeMessageGateway> channelsWithoutCredit, String email) {
        if (channelsWithoutCredit.isEmpty()) return;

        String emailTitle = "Smart Payments - Créditos esgotados";

        CustomMessage customMessageInput = mountCustomMessageGatewayInput(
            EChannelTypeMessageGateway.EMAIL,
            NO_CREDITS_AVAILABLE_EMAIL_TEMPLATE,
            null,
            emailTitle,
            email,
            mountNoCreditsEmailVariables(channelsWithoutCredit)
        );

        asyncMessages.add(customMessageInput);
    }

    private void sendAsyncMessageGatewayMessages(LinkedHashSet<com.messaging_gateway.avro.CustomMessage> asyncMessages) {
        asyncMessages.forEach(message -> kafkaAvroTemplate.send(messageGatewayTopic, message));
    }

    private PaymentScheduledNotification findNotification(Long id) {
        return paymentScheduledNotificationDataProviderPort.findByIdWithReceivers(id)
            .orElseThrow(PaymentScheduledNotificationNotFoundException::new);
    }

    private UserSubscriptionOutput findUserSubscription(Long userId) {
        return subscriptionClientPort.getUserSubscription(userId);
    }
}
