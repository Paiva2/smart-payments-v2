package com.smartpayments.scheduler.core.domain.usecase.paymentScheduledNotification.listPaymentScheduledNotificatios;

import com.smartpayments.scheduler.core.common.exception.InvalidInputException;
import com.smartpayments.scheduler.core.domain.model.PaymentScheduledNotification;
import com.smartpayments.scheduler.core.ports.in.usecase.UsecasePort;
import com.smartpayments.scheduler.core.ports.in.usecase.dto.ListPaymentScheduledNotificationFilter;
import com.smartpayments.scheduler.core.ports.out.dataProvider.PaymentScheduledNotificationDataProviderPort;
import com.smartpayments.scheduler.core.ports.out.usecase.dto.ListPaymentScheduledNotificationOutput;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;

import static java.util.Objects.isNull;

@Service
@AllArgsConstructor
public class ListPaymentScheduledNotificationsUsecase implements UsecasePort<ListPaymentScheduledNotificationFilter, ListPaymentScheduledNotificationOutput> {
    public static final Integer MAX_DAYS_DATE_BETWEEN = 180;

    private final PaymentScheduledNotificationDataProviderPort paymentScheduledNotificationDataProviderPort;

    @Override
    @Transactional(readOnly = true)
    public ListPaymentScheduledNotificationOutput execute(ListPaymentScheduledNotificationFilter input) {
        formatPaginationInput(input);
        validateDate(input);
        Slice<PaymentScheduledNotification> paymentScheduledNotifications = findNotificationsScheduled(input);

        return new ListPaymentScheduledNotificationOutput(paymentScheduledNotifications);
    }

    private void validateDate(ListPaymentScheduledNotificationFilter input) {
        if (isNull(input.getEndDate())) return;

        if (input.getStartDate().isAfter(input.getEndDate())) {
            throw new InvalidInputException("Start date can't be after end date!");
        }

        long daysBetweenDates = ChronoUnit.DAYS.between(
            input.getStartDate(),
            input.getEndDate()
        );

        if (daysBetweenDates > MAX_DAYS_DATE_BETWEEN) {
            throw new InvalidInputException("Start and end date can't be more than " + MAX_DAYS_DATE_BETWEEN + " days apart!");
        }
    }

    private void formatPaginationInput(ListPaymentScheduledNotificationFilter input) {
        if (input.getPage() < 0) {
            input.setPage(0);
        }

        if (input.getPageSize() > 100) {
            input.setPageSize(100);
        }

        if (input.getPageSize() < 5) {
            input.setPageSize(5);
        }
    }

    private Slice<PaymentScheduledNotification> findNotificationsScheduled(ListPaymentScheduledNotificationFilter input) {
        return paymentScheduledNotificationDataProviderPort.findPaymentNotificationsFiltering(
            input.getUserId(),
            input.getPage(),
            input.getPageSize(),
            input.getSortDirection(),
            input.getSortBy(),
            input.getTitle(),
            input.getRecurrence(),
            input.getStartDate(),
            input.getEndDate(),
            input.getNextDate(),
            input.getStatus(),
            input.getReceiver()
        );
    }
}
