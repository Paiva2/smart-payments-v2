package com.smartpayments.scheduler.core.domain.usecase.paymentScheduledNotification.editPaymentScheduledNotification;

import com.smartpayments.scheduler.core.common.exception.PaymentScheduledNotificationCancelledException;
import com.smartpayments.scheduler.core.common.exception.PaymentScheduledNotificationNotFoundException;
import com.smartpayments.scheduler.core.domain.enums.ENotificationRecurrence;
import com.smartpayments.scheduler.core.domain.enums.ENotificationScheduleStatus;
import com.smartpayments.scheduler.core.domain.model.PaymentReceiver;
import com.smartpayments.scheduler.core.domain.model.PaymentScheduledNotification;
import com.smartpayments.scheduler.core.domain.usecase.paymentScheduledNotification.schedulePaymentNotification.exception.InvalidPaymentScheduledDateException;
import com.smartpayments.scheduler.core.domain.usecase.paymentScheduledNotification.schedulePaymentNotification.exception.ScheduledPaymentNotificationAlreadyExists;
import com.smartpayments.scheduler.core.ports.in.usecase.UsecasePort;
import com.smartpayments.scheduler.core.ports.in.usecase.dto.EditPaymentScheduledNotificationInput;
import com.smartpayments.scheduler.core.ports.out.dataProvider.PaymentScheduledNotificationDataProviderPort;
import com.smartpayments.scheduler.core.ports.out.usecase.dto.PaymentReceiverOutput;
import com.smartpayments.scheduler.core.ports.out.usecase.dto.PaymentScheduledNotificationOutput;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static java.util.Objects.nonNull;
import static org.springframework.util.ObjectUtils.isEmpty;

@Service
@AllArgsConstructor
public class EditPaymentScheduledNotificationUsecase implements UsecasePort<EditPaymentScheduledNotificationInput, PaymentScheduledNotificationOutput> {
    private final PaymentScheduledNotificationDataProviderPort paymentScheduledNotificationDataProviderPort;

    @Override
    @Transactional
    public PaymentScheduledNotificationOutput execute(EditPaymentScheduledNotificationInput input) {
        PaymentScheduledNotification paymentScheduled = findPaymentScheduled(input.getId());

        checkPaymentBelongsToUser(paymentScheduled, input.getUserId());

        if (paymentScheduled.getStatus().equals(ENotificationScheduleStatus.CANCELLED)) {
            throw new PaymentScheduledNotificationCancelledException("Can't edit a cancelled notification!");
        }

        verifyTitleAlreadyExists(paymentScheduled, input);

        editPaymentScheduled(paymentScheduled, input);
        paymentScheduled = persistPaymentScheduled(paymentScheduled);

        return mountOutput(paymentScheduled);
    }

    private PaymentScheduledNotification findPaymentScheduled(Long id) {
        return paymentScheduledNotificationDataProviderPort.findByIdWithReceivers(id)
            .orElseThrow(PaymentScheduledNotificationNotFoundException::new);
    }

    private void checkPaymentBelongsToUser(PaymentScheduledNotification paymentScheduled, Long userId) {
        if (!paymentScheduled.getUserId().equals(userId)) {
            throw new PaymentScheduledNotificationNotFoundException();
        }
    }

    private void verifyTitleAlreadyExists(PaymentScheduledNotification paymentScheduledNotification, EditPaymentScheduledNotificationInput input) {
        if (paymentScheduledNotification.getTitle().equals(input.getTitle())) return;

        paymentScheduledNotificationDataProviderPort.findByTitleAndUserId(input.getTitle(), input.getUserId())
            .ifPresent(existing -> {
                throw new ScheduledPaymentNotificationAlreadyExists(
                    String.format("Payment with provided title already exists for this user! Title: %s", existing.getTitle())
                );
            });
    }

    private void editPaymentScheduled(PaymentScheduledNotification paymentScheduledNotification, EditPaymentScheduledNotificationInput input) {
        Date originalStartDate = resetDateSeconds(paymentScheduledNotification.getStartDate());
        ENotificationRecurrence originalRecurrence = paymentScheduledNotification.getRecurrence();

        paymentScheduledNotification.setTitle(!isEmpty(input.getTitle()) ? input.getTitle() : paymentScheduledNotification.getTitle());
        paymentScheduledNotification.setDescription(!isEmpty(input.getDescription()) ? input.getDescription() : paymentScheduledNotification.getDescription());
        paymentScheduledNotification.setValue(!isEmpty(input.getValue()) ? input.getValue() : paymentScheduledNotification.getValue());
        paymentScheduledNotification.setRecurrence(!isEmpty(input.getRecurrence()) ? input.getRecurrence() : paymentScheduledNotification.getRecurrence());
        paymentScheduledNotification.setStartDate(!isEmpty(input.getStartDate()) ? input.getStartDate() : paymentScheduledNotification.getStartDate());
        paymentScheduledNotification.setEndDate(input.getEndDate());
        paymentScheduledNotification.setNotifyWhatsApp(!isEmpty(input.getNotifyWhatsApp()) ? input.getNotifyWhatsApp() : paymentScheduledNotification.getNotifyWhatsApp());
        paymentScheduledNotification.setNotifyEmail(!isEmpty(input.getNotifyEmail()) ? input.getNotifyEmail() : paymentScheduledNotification.getNotifyEmail());
        paymentScheduledNotification.setNotifySms(!isEmpty(input.getNotifySms()) ? input.getNotifySms() : paymentScheduledNotification.getNotifySms());

        boolean hasChangedStartDate = !paymentScheduledNotification.getStartDate().equals(originalStartDate);
        boolean hasChangedRecurrence = !paymentScheduledNotification.getRecurrence().equals(originalRecurrence);

        handleStartDate(hasChangedStartDate, paymentScheduledNotification);
        handleEndDate(paymentScheduledNotification);

        if (hasChangedRecurrence) {
            if (nonNull(paymentScheduledNotification.getNextDate())) {
                paymentScheduledNotification.setNextDate(null);
            }
        }

        if (nonNull(input.getReceivers())) {
            handlePaymentReceivers(input, paymentScheduledNotification);
        }
    }

    private void handleStartDate(boolean hasChangedStartDate, PaymentScheduledNotification paymentScheduledNotification) {
        if (hasChangedStartDate) {
            Date startDate = resetDateSeconds(paymentScheduledNotification.getStartDate());
            Date now = resetDateSeconds(new Date());

            if (startDate.before(now)) {
                throw new InvalidPaymentScheduledDateException("Start date can't be before today!");
            }

            if (nonNull(paymentScheduledNotification.getNextDate()) && paymentScheduledNotification.getNextDate().before(paymentScheduledNotification.getStartDate())) {
                paymentScheduledNotification.setNextDate(null);
                paymentScheduledNotification.setStatus(ENotificationScheduleStatus.ACTIVE);
            }
        }
    }

    private void handleEndDate(PaymentScheduledNotification paymentScheduledNotification) {
        if (nonNull(paymentScheduledNotification.getEndDate())) {
            final Date endDate = resetDateSeconds(paymentScheduledNotification.getEndDate());

            if (endDate.before(paymentScheduledNotification.getStartDate())) {
                throw new InvalidPaymentScheduledDateException("End date can't be before start date!");
            }

            if (nonNull(paymentScheduledNotification.getNextDate())) {
                if (paymentScheduledNotification.getNextDate().after(endDate)) {
                    paymentScheduledNotification.setStatus(ENotificationScheduleStatus.PAUSED);
                } else {
                    paymentScheduledNotification.setStatus(ENotificationScheduleStatus.ACTIVE);
                }
            }
        } else if (paymentScheduledNotification.getStatus().equals(ENotificationScheduleStatus.PAUSED)) {
            paymentScheduledNotification.setStatus(ENotificationScheduleStatus.ACTIVE);
        }
    }

    private Date resetDateSeconds(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private void handlePaymentReceivers(EditPaymentScheduledNotificationInput input, PaymentScheduledNotification paymentScheduledNotification) {
        if (input.getReceivers().isEmpty()) return;
        List<PaymentReceiver> paymentReceivers = paymentScheduledNotification.getReceivers();

        paymentReceivers.clear();

        for (EditPaymentScheduledNotificationInput.ReceiverInput receiver : input.getReceivers()) {
            paymentReceivers.add(PaymentReceiver.builder()
                .identification(receiver.getIdentification().trim())
                .paymentScheduledNotification(paymentScheduledNotification)
                .build()
            );
        }
    }

    private PaymentScheduledNotification persistPaymentScheduled(PaymentScheduledNotification paymentScheduledNotification) {
        return paymentScheduledNotificationDataProviderPort.persist(paymentScheduledNotification);
    }

    private PaymentScheduledNotificationOutput mountOutput(PaymentScheduledNotification paymentScheduled) {
        return PaymentScheduledNotificationOutput.builder()
            .id(paymentScheduled.getId())
            .title(paymentScheduled.getTitle())
            .description(paymentScheduled.getDescription())
            .value(paymentScheduled.getValue())
            .recurrence(paymentScheduled.getRecurrence())
            .startDate(paymentScheduled.getStartDate())
            .endDate(paymentScheduled.getEndDate())
            .nextDate(paymentScheduled.getNextDate())
            .lastDate(paymentScheduled.getLastDate())
            .lastExecutionStatus(paymentScheduled.getLastExecutionStatus())
            .status(paymentScheduled.getStatus())
            .notifyWhatsApp(paymentScheduled.getNotifyWhatsApp())
            .notifyEmail(paymentScheduled.getNotifyEmail())
            .notifySms(paymentScheduled.getNotifySms())
            .receivers(paymentScheduled.getReceivers().stream()
                .map(rc -> PaymentReceiverOutput.builder()
                    .id(rc.getId())
                    .identification(rc.getIdentification())
                    .build()
                ).toList()
            ).build();
    }
}
