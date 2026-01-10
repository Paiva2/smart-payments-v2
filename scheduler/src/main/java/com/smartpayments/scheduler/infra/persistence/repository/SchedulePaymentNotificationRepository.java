package com.smartpayments.scheduler.infra.persistence.repository;

import com.smartpayments.scheduler.core.domain.model.PaymentScheduledNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SchedulePaymentNotificationRepository extends JpaRepository<PaymentScheduledNotification, Long> {

    @Query("select psn from PaymentScheduledNotification psn where psn.userId = :userId and lower(psn.title) = :title")
    Optional<PaymentScheduledNotification> findByTitleAndUser(@Param("userId") Long userId, @Param("title") String title);
}
