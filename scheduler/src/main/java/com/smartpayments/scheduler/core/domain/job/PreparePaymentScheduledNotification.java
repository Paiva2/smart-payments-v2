package com.smartpayments.scheduler.core.domain.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartpayments.scheduler.core.domain.enums.ENotificationScheduleStatus;
import com.smartpayments.scheduler.core.domain.model.PaymentScheduledNotification;
import com.smartpayments.scheduler.core.ports.in.external.messaging.AsyncMessageInput;
import com.smartpayments.scheduler.core.ports.in.usecase.dto.ProcessPaymentScheduledNotificationInput;
import com.smartpayments.scheduler.core.ports.out.dataProvider.PaymentScheduledNotificationDataProviderPort;
import com.smartpayments.scheduler.core.ports.out.utils.MessageUtilsPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static java.util.Objects.nonNull;

@Slf4j
@Component
@RequiredArgsConstructor
public class PreparePaymentScheduledNotification {
    private final static ObjectMapper mapper = new ObjectMapper();

    private final PaymentScheduledNotificationDataProviderPort paymentScheduledNotificationDataProviderPort;

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final MessageUtilsPort messageUtilsPort;

    @Value("${spring.kafka.topics.process-scheduled-payment-notifications}")
    private String processNotificationTopic;

    @Scheduled(cron = "0 */5 * * * *")
    public void execute() {
        log.info("[PreparePaymentScheduledNotification#execute] - Preparing scheduled notifications");

        try {
            List<PaymentScheduledNotification> paymentScheduledNotifications = findAllToNotify();
            paymentScheduledNotifications.forEach(this::handleNotification);
            log.info("[PreparePaymentScheduledNotification#execute] - Send all notifications scheduled to be sent");
        } catch (Exception e) {
            log.error("[PreparePaymentScheduledNotification#execute] - Error while preparing scheduled notification", e);
        }
    }

    private List<PaymentScheduledNotification> findAllToNotify() {
        return paymentScheduledNotificationDataProviderPort.findAllToNotify();
    }

    private void handleNotification(PaymentScheduledNotification paymentScheduledNotification) {
        Date nextDate = defineNextPaymentNotification(paymentScheduledNotification);

        if (nonNull(paymentScheduledNotification.getEndDate()) && paymentScheduledNotification.getEndDate().before(nextDate)) {
            paymentScheduledNotification.setStatus(ENotificationScheduleStatus.PAUSED);
        }

        paymentScheduledNotification.setNextDate(nextDate);
        paymentScheduledNotification.setLastDate(new Date());

        paymentScheduledNotificationDataProviderPort.persist(paymentScheduledNotification);

        sendNotificationToProcess(paymentScheduledNotification);
    }

    private Date defineNextPaymentNotification(PaymentScheduledNotification psn) {
        Calendar calendar = Calendar.getInstance();

        Date baseDate = nonNull(psn.getNextDate()) ? psn.getNextDate() : psn.getStartDate();

        calendar.setTime(baseDate);

        switch (psn.getRecurrence()) {
            case DAILY -> calendar.add(Calendar.DAY_OF_MONTH, 1);
            case WEEKLY -> calendar.add(Calendar.DAY_OF_MONTH, 7);
            case MONTHLY -> calendar.add(Calendar.MONTH, 1);
            case YEARLY -> calendar.add(Calendar.YEAR, 1);
        }

        return calendar.getTime();
    }

    private void sendNotificationToProcess(PaymentScheduledNotification paymentScheduledNotification) {
        try {
            String messageHash = messageUtilsPort.generateMessageHash("SCHEDULER");

            ProcessPaymentScheduledNotificationInput processScheduledNotificationInput = new ProcessPaymentScheduledNotificationInput(
                messageHash,
                paymentScheduledNotification.getId()
            );

            AsyncMessageInput asyncMessageInput = AsyncMessageInput.builder()
                .messageHash(messageHash)
                .data(processScheduledNotificationInput)
                .timestamp(new Date())
                .issuer("SCHEDULER")
                .build();

            kafkaTemplate.send(
                processNotificationTopic,
                paymentScheduledNotification.getId().toString(),
                mapper.writeValueAsString(asyncMessageInput)
            );
        } catch (Exception exception) {
            log.error("[PreparePaymentScheduledNotification#sendNotificationToProcess] - Error while sending notification to process", exception);
        }
    }
}
