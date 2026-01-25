package com.smartpayments.scheduler.core.domain.usecase.paymentScheduledNotification.cancelPaymentScheduledNotification;

import com.smartpayments.scheduler.core.common.exception.PaymentScheduledNotificationNotFoundException;
import com.smartpayments.scheduler.core.domain.enums.ENotificationScheduleStatus;
import com.smartpayments.scheduler.core.domain.model.PaymentScheduledNotification;
import com.smartpayments.scheduler.core.domain.usecase.paymentScheduledNotification.cancelPaymentScheduledNotification.exception.PaymentScheduledNotificationAlreadyCancelledException;
import com.smartpayments.scheduler.core.ports.in.usecase.UsecaseVoidPort;
import com.smartpayments.scheduler.core.ports.in.usecase.dto.CancelPaymentScheduledNotificationInput;
import com.smartpayments.scheduler.core.ports.out.dataProvider.PaymentScheduledNotificationDataProviderPort;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CancelPaymentScheduledNotificationUsecase implements UsecaseVoidPort<CancelPaymentScheduledNotificationInput> {
    private final PaymentScheduledNotificationDataProviderPort paymentScheduledNotificationDataProviderPort;

    @Override
    public void execute(CancelPaymentScheduledNotificationInput input) {
        PaymentScheduledNotification paymentScheduledNotification = findPaymentScheduled(input);

        if (!paymentScheduledNotification.getStatus().equals(ENotificationScheduleStatus.ACTIVE)) {
            throw new PaymentScheduledNotificationAlreadyCancelledException();
        }

        updatePaymentScheduled(paymentScheduledNotification);
        persistPaymentScheduled(paymentScheduledNotification);
    }

    public PaymentScheduledNotification findPaymentScheduled(CancelPaymentScheduledNotificationInput input) {
        return paymentScheduledNotificationDataProviderPort.findByIdAndUserIdLocking(input.getId(), input.getUserId())
            .orElseThrow(PaymentScheduledNotificationNotFoundException::new);
    }

    private void updatePaymentScheduled(PaymentScheduledNotification paymentScheduledNotification) {
        paymentScheduledNotification.setNextDate(null);
        paymentScheduledNotification.setStatus(ENotificationScheduleStatus.CANCELLED);
    }

    private void persistPaymentScheduled(PaymentScheduledNotification paymentScheduledNotification) {
        paymentScheduledNotificationDataProviderPort.persist(paymentScheduledNotification);
    }
}
