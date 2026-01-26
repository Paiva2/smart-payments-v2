package com.smartpayments.scheduler.core.domain.usecase.paymentScheduledNotification.resumeCancelledPaymentScheduledNotification;

import com.smartpayments.scheduler.core.common.exception.PaymentScheduledNotificationNotFoundException;
import com.smartpayments.scheduler.core.domain.enums.ENotificationScheduleStatus;
import com.smartpayments.scheduler.core.domain.model.PaymentScheduledNotification;
import com.smartpayments.scheduler.core.domain.usecase.paymentScheduledNotification.resumeCancelledPaymentScheduledNotification.exception.ResumeCancelledPaymentScheduledNotificationException;
import com.smartpayments.scheduler.core.ports.in.usecase.UsecaseVoidPort;
import com.smartpayments.scheduler.core.ports.in.usecase.dto.ResumeCancelledPaymentScheduledNotificationInput;
import com.smartpayments.scheduler.core.ports.out.dataProvider.PaymentScheduledNotificationDataProviderPort;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

import static java.util.Objects.nonNull;

@Service
@AllArgsConstructor
public class ResumeCancelledPaymentScheduledNotificationUsecase implements UsecaseVoidPort<ResumeCancelledPaymentScheduledNotificationInput> {
    private final PaymentScheduledNotificationDataProviderPort paymentScheduledNotificationDataProviderPort;

    @Override
    public void execute(ResumeCancelledPaymentScheduledNotificationInput input) {
        PaymentScheduledNotification paymentScheduledNotification = findPaymentScheduled(input);

        if (!paymentScheduledNotification.getStatus().equals(ENotificationScheduleStatus.CANCELLED)) {
            throw new ResumeCancelledPaymentScheduledNotificationException("Payment notification is not cancelled!");
        }

        updatePaymentScheduled(paymentScheduledNotification);
        persistPaymentScheduled(paymentScheduledNotification);
    }

    public PaymentScheduledNotification findPaymentScheduled(ResumeCancelledPaymentScheduledNotificationInput input) {
        return paymentScheduledNotificationDataProviderPort.findByIdAndUserIdLocking(input.getId(), input.getUserId())
            .orElseThrow(PaymentScheduledNotificationNotFoundException::new);
    }

    private void updatePaymentScheduled(PaymentScheduledNotification paymentScheduledNotification) {
        if (nonNull(paymentScheduledNotification.getEndDate()) && paymentScheduledNotification.getEndDate().before(new Date())) {
            paymentScheduledNotification.setStatus(ENotificationScheduleStatus.PAUSED);
        } else {
            paymentScheduledNotification.setStatus(ENotificationScheduleStatus.ACTIVE);
        }
    }

    private void persistPaymentScheduled(PaymentScheduledNotification paymentScheduledNotification) {
        paymentScheduledNotificationDataProviderPort.persist(paymentScheduledNotification);
    }
}
