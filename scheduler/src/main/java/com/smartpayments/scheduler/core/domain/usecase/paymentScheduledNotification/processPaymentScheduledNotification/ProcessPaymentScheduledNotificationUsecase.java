package com.smartpayments.scheduler.core.domain.usecase.paymentScheduledNotification.processPaymentScheduledNotification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartpayments.scheduler.core.common.enums.EChannelTypeMessageGateway;
import com.smartpayments.scheduler.core.common.exception.PaymentScheduledNotificationNotFoundException;
import com.smartpayments.scheduler.core.domain.enums.ENotificationExecutionStatus;
import com.smartpayments.scheduler.core.domain.model.PaymentReceiver;
import com.smartpayments.scheduler.core.domain.model.PaymentScheduledNotification;
import com.smartpayments.scheduler.core.domain.usecase.paymentScheduledNotification.processPaymentScheduledNotification.exception.ProcessPaymentScheduledNotificationException;
import com.smartpayments.scheduler.core.ports.in.external.messaging.AsyncMessageInput;
import com.smartpayments.scheduler.core.ports.in.external.messaging.UserSubscriptionCreditInput;
import com.smartpayments.scheduler.core.ports.in.usecase.UsecaseVoidPort;
import com.smartpayments.scheduler.core.ports.in.usecase.dto.ProcessPaymentScheduledNotificationInput;
import com.smartpayments.scheduler.core.ports.out.dataProvider.PaymentScheduledNotificationDataProviderPort;
import com.smartpayments.scheduler.core.ports.out.external.dto.AsyncMessageGatewayInput;
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
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${spring.kafka.topics.messaging-gateway}")
    private String messageGatewayTopic;

    @Override
    @Transactional
    public void execute(ProcessPaymentScheduledNotificationInput input) {
        LinkedHashSet<AsyncMessageInput> asyncMessages = new LinkedHashSet<>();
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

    private AsyncMessageGatewayInput mountAsyncMessageGatewayInput(EChannelTypeMessageGateway channelType, String template, String message, String subject, String to, HashMap<String, Object> variables) {
        return AsyncMessageGatewayInput.builder()
            .channelType(channelType)
            .subject(subject)
            .template(template)
            .to(to)
            .variables(variables)
            .message(message)
            .build();
    }

    private HashMap<String, Object> mountNoCreditsEmailVariables(List<EChannelTypeMessageGateway> channelTypes) {
        return new HashMap<>() {{
            put("${PAYMENT_NOTIFICATION_TYPE}", String.join(", ", channelTypes.stream().map(EChannelTypeMessageGateway::getType).toList()));
        }};
    }

    private HashMap<String, Object> mountPaymentNotificationWhatsAppVariables(UserSubscriptionOutput userSubscriptionOutput, PaymentScheduledNotification paymentScheduledNotification) {
        return new HashMap<>() {{
            put("${USER_NAME}", userSubscriptionOutput.getFirstName());
            put("${PAYMENT_NOTIFICATION_TITLE}", paymentScheduledNotification.getTitle());
            put("${PAYMENT_NOTIFICATION_DESCRIPTION}", paymentScheduledNotification.getDescription());
            put("${PAYMENT_NOTIFICATION_VALUE}", paymentScheduledNotification.getValue());
            put("${PAYMENT_NOTIFICATION_RECEIVERS_LIST}", String.join(", ", paymentScheduledNotification.getReceivers().stream().map(PaymentReceiver::getIdentification).toList()));
        }};
    }

    private HashMap<String, Object> mountPaymentNotificationSmsVariables(UserSubscriptionOutput userSubscriptionOutput, PaymentScheduledNotification paymentScheduledNotification) {
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

    private HashMap<String, Object> mountPaymentNotificationEmailVariables(UserSubscriptionOutput userSubscriptionOutput, PaymentScheduledNotification paymentScheduledNotification) {
        return new HashMap<>() {{
            put("${USER_NAME}", userSubscriptionOutput.getFirstName());
            put("${PAYMENT_NOTIFICATION_TITLE}", paymentScheduledNotification.getTitle());
            put("${PAYMENT_NOTIFICATION_DESCRIPTION}", paymentScheduledNotification.getDescription());
            put("${PAYMENT_NOTIFICATION_VALUE}", paymentScheduledNotification.getValue());
            put("${PAYMENT_NOTIFICATION_NEXT_NOTIFICATION_DATE}", paymentScheduledNotification.getNextDate());
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
        HashMap<String, Object> variables,
        LinkedHashSet<AsyncMessageInput> asyncMessages
    ) {
        AsyncMessageGatewayInput messageGatewayInput = mountAsyncMessageGatewayInput(
            channel,
            template,
            message,
            title,
            to,
            variables
        );

        asyncMessages.add(AsyncMessageInput.builder()
            .messageHash(messageUtilsPort.generateMessageHash(ISSUER))
            .data(messageGatewayInput)
            .issuer(ISSUER)
            .timestamp(new Date())
            .build()
        );
    }

    private ConsumeUserSubscriptionCreditsOutput handleConsumeCreditsExternal(String messageHash, UserSubscriptionCreditInput input) {
        return subscriptionClientPort.consumeUserSubscriptionCredits(messageHash, input);
    }

    private void fillNoCreditMessages(LinkedHashSet<AsyncMessageInput> asyncMessages, List<EChannelTypeMessageGateway> channelsWithoutCredit, String email) {
        if (channelsWithoutCredit.isEmpty()) return;

        String emailTitle = "Smart Payments - Créditos esgotados";

        AsyncMessageGatewayInput messageGatewayInput = mountAsyncMessageGatewayInput(
            EChannelTypeMessageGateway.EMAIL,
            NO_CREDITS_AVAILABLE_EMAIL_TEMPLATE,
            null,
            emailTitle,
            email,
            mountNoCreditsEmailVariables(channelsWithoutCredit)
        );

        asyncMessages.add(AsyncMessageInput.builder()
            .messageHash(messageUtilsPort.generateMessageHash(ISSUER))
            .data(messageGatewayInput)
            .issuer(ISSUER)
            .timestamp(new Date())
            .build()
        );
    }

    private void sendAsyncMessageGatewayMessages(LinkedHashSet<AsyncMessageInput> asyncMessages) {
        List<String> asyncMessagesString = asyncMessages.stream().map(asyncMessage -> {
            try {
                return mapper.writeValueAsString(asyncMessage);
            } catch (JsonProcessingException e) {
                log.error("[ProcessPaymentScheduledNotificationUsecase#sendAsyncMessageGatewayMessages] - Error while converting async messages  to string!", e);
                throw new ProcessPaymentScheduledNotificationException("Error while converting messages to be sent!");
            }
        }).toList();

        asyncMessagesString.forEach(message -> kafkaTemplate.send(messageGatewayTopic, message));
    }

    private PaymentScheduledNotification findNotification(Long id) {
        return paymentScheduledNotificationDataProviderPort.findByIdWithReceivers(id)
            .orElseThrow(PaymentScheduledNotificationNotFoundException::new);
    }

    private UserSubscriptionOutput findUserSubscription(Long userId) {
        return subscriptionClientPort.getUserSubscription(userId);
    }
}
