package com.smartpayments.scheduler.infra.adapters.dataProvider;

import com.smartpayments.scheduler.core.domain.model.PaymentScheduledNotification;
import com.smartpayments.scheduler.core.ports.out.dataProvider.PaymentScheduledNotificationDataProviderPort;
import com.smartpayments.scheduler.infra.persistence.repository.SchedulePaymentNotificationRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

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
    public Optional<PaymentScheduledNotification> findByIdWithReceivers(Long id) {
        return repository.findByIdWithReceivers(id);
    }

    @Override
    public List<PaymentScheduledNotification> findAllToNotify() {
        return repository.findAllToPrepareNotification();
    }
}
