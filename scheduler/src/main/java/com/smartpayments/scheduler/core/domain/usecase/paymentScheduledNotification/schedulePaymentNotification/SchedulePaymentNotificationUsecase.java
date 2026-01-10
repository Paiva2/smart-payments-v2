package com.smartpayments.scheduler.core.domain.usecase.paymentScheduledNotification.schedulePaymentNotification;

import com.smartpayments.scheduler.core.domain.enums.ENotificationScheduleStatus;
import com.smartpayments.scheduler.core.domain.model.PaymentReceiver;
import com.smartpayments.scheduler.core.domain.model.PaymentScheduledNotification;
import com.smartpayments.scheduler.core.domain.usecase.paymentScheduledNotification.schedulePaymentNotification.exception.InvalidPaymentScheduledDateException;
import com.smartpayments.scheduler.core.domain.usecase.paymentScheduledNotification.schedulePaymentNotification.exception.ScheduledPaymentNotificationAlreadyExists;
import com.smartpayments.scheduler.core.ports.in.usecase.UsecaseVoidPort;
import com.smartpayments.scheduler.core.ports.in.usecase.dto.SchedulePaymentNotificationInput;
import com.smartpayments.scheduler.core.ports.out.dataProvider.PaymentScheduledNotificationDataProviderPort;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

import static java.util.Objects.nonNull;

@Service
@AllArgsConstructor
public class SchedulePaymentNotificationUsecase implements UsecaseVoidPort<SchedulePaymentNotificationInput> {
    private final PaymentScheduledNotificationDataProviderPort paymentScheduledNotificationDataProviderPort;

    @Override
    public void execute(SchedulePaymentNotificationInput input) {
        validateStartEndDate(input);

        verifyTitleAlreadyExists(input);

        PaymentScheduledNotification scheduledNotification = fillPaymentScheduledNotification(input);
        persistScheduledPaymentNotification(scheduledNotification);
    }

    private void validateStartEndDate(SchedulePaymentNotificationInput input) {
        final Date now = resetDateSeconds(new Date());
        final Date startDate = resetDateSeconds(input.getStartDate());

        if (startDate.before(now)) {
            throw new InvalidPaymentScheduledDateException("Start date can't be before today!");
        }

        if (nonNull(input.getEndDate())) {
            final Date endDate = resetDateSeconds(input.getEndDate());

            if (endDate.before(startDate)) {
                throw new InvalidPaymentScheduledDateException("End date can't be before start date!");
            }
        }
    }

    private void verifyTitleAlreadyExists(SchedulePaymentNotificationInput input) {
        paymentScheduledNotificationDataProviderPort.findByTitleAndUserId(input.getTitle(), input.getUserId())
            .ifPresent(existing -> {
                throw new ScheduledPaymentNotificationAlreadyExists(
                    String.format("Payment with provided title already exists for this user! Title: %s", existing.getTitle())
                );
            });
    }

    private Date resetDateSeconds(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private PaymentScheduledNotification fillPaymentScheduledNotification(SchedulePaymentNotificationInput input) {
        PaymentScheduledNotification paymentScheduledNotification = PaymentScheduledNotification.builder()
            .title(input.getTitle())
            .description(input.getDescription())
            .value(input.getValue())
            .recurrence(input.getRecurrence())
            .startDate(input.getStartDate())
            .endDate(input.getEndDate())
            .status(ENotificationScheduleStatus.ACTIVE)
            .notifyWhatsApp(input.getNotifyWhatsApp())
            .notifyEmail(input.getNotifyEmail())
            .notifySms(input.getNotifySms())
            .userId(input.getUserId())
            .build();

        paymentScheduledNotification.setReceivers(fillReceivers(input, paymentScheduledNotification));

        return paymentScheduledNotification;
    }

    private List<PaymentReceiver> fillReceivers(SchedulePaymentNotificationInput input, PaymentScheduledNotification paymentScheduledNotification) {
        Set<PaymentReceiver> receivers = new HashSet<>();

        for (SchedulePaymentNotificationInput.ReceiverInput receiverInput : input.getReceivers()) {
            receivers.add(PaymentReceiver.builder()
                .identification(receiverInput.getIdentification().trim())
                .paymentScheduledNotification(paymentScheduledNotification)
                .build()
            );
        }

        return new ArrayList<>(receivers);
    }

    private void persistScheduledPaymentNotification(PaymentScheduledNotification paymentScheduledNotification) {
        paymentScheduledNotificationDataProviderPort.persist(paymentScheduledNotification);
    }
}
