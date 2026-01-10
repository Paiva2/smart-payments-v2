package com.smartpayments.scheduler.core.ports.out.dataProvider;

import com.smartpayments.scheduler.core.domain.model.PaymentScheduledNotification;

import java.util.Optional;

public interface PaymentScheduledNotificationDataProviderPort {
    PaymentScheduledNotification persist(PaymentScheduledNotification paymentScheduledNotification);

    Optional<PaymentScheduledNotification> findByTitleAndUserId(String title, Long userId);
}
