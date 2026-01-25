package com.smartpayments.scheduler.infra.adapters.dataProvider;

import com.smartpayments.scheduler.core.domain.enums.ENotificationRecurrence;
import com.smartpayments.scheduler.core.domain.enums.ENotificationScheduleStatus;
import com.smartpayments.scheduler.core.domain.model.PaymentScheduledNotification;
import com.smartpayments.scheduler.core.ports.out.dataProvider.PaymentScheduledNotificationDataProviderPort;
import com.smartpayments.scheduler.infra.persistence.repository.SchedulePaymentNotificationRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Component
@AllArgsConstructor
public class PaymentScheduledNotificationDataProviderAdapter implements PaymentScheduledNotificationDataProviderPort {
    private final SchedulePaymentNotificationRepository repository;

    @Override
    public PaymentScheduledNotification persist(PaymentScheduledNotification paymentScheduledNotification) {
        return repository.save(paymentScheduledNotification);
    }

    @Override
    public Optional<PaymentScheduledNotification> findByTitleAndUserId(String title, Long userId) {
        return repository.findByTitleAndUser(userId, title.toLowerCase(Locale.ROOT));
    }

    @Override
    public Optional<PaymentScheduledNotification> findByIdAndUserIdLocking(Long id, Long userId) {
        return repository.findByIdAndUserIdLocking(id, userId);
    }

    @Override
    public Optional<PaymentScheduledNotification> findByIdWithReceivers(Long id) {
        return repository.findByIdWithReceivers(id);
    }

    @Override
    public List<PaymentScheduledNotification> findAllToNotify() {
        return repository.findAllToPrepareNotification();
    }

    @Override
    public Slice<PaymentScheduledNotification> findPaymentNotificationsFiltering(
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
    ) {
        PageRequest pageRequest = PageRequest.of(page, pageSize, sortDirection, sortBy);

        return repository.findPaymentNotificationsFiltering(
            userId,
            title,
            recurrence == null ? null : recurrence.name(),
            startDate,
            endDate,
            nextDate,
            status == null ? null : status.name(),
            receiver,
            pageRequest
        );
    }
}
