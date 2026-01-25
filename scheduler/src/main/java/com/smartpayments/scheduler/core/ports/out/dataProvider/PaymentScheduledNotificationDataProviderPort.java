package com.smartpayments.scheduler.core.ports.out.dataProvider;

import com.smartpayments.scheduler.core.domain.enums.ENotificationRecurrence;
import com.smartpayments.scheduler.core.domain.enums.ENotificationScheduleStatus;
import com.smartpayments.scheduler.core.domain.model.PaymentScheduledNotification;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PaymentScheduledNotificationDataProviderPort {
    PaymentScheduledNotification persist(PaymentScheduledNotification paymentScheduledNotification);

    Optional<PaymentScheduledNotification> findByTitleAndUserId(String title, Long userId);

    Optional<PaymentScheduledNotification> findByIdWithReceivers(Long id);

    List<PaymentScheduledNotification> findAllToNotify();

    Slice<PaymentScheduledNotification> findPaymentNotificationsFiltering(
        Long userId,
        Integer page,
        Integer pageSize,
        Sort.Direction sortDirection,
        String sortBy,
        String title,
        ENotificationRecurrence recurrence,
        LocalDate startDate,
        LocalDate endDate,
        LocalDate nextDate,
        ENotificationScheduleStatus status,
        String receiver
    );
}
