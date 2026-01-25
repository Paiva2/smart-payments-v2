package com.smartpayments.scheduler.infra.persistence.repository;

import com.smartpayments.scheduler.core.domain.model.PaymentScheduledNotification;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SchedulePaymentNotificationRepository extends JpaRepository<PaymentScheduledNotification, Long> {

    @Query("select psn from PaymentScheduledNotification psn where psn.userId = :userId and lower(psn.title) = :title")
    Optional<PaymentScheduledNotification> findByTitleAndUser(@Param("userId") Long userId, @Param("title") String title);

    @Query("select psn from PaymentScheduledNotification psn join psn.receivers where psn.id = :id")
    Optional<PaymentScheduledNotification> findByIdWithReceivers(@Param("id") Long id);

    @Query(value = """
        select * from payment_scheduled_notification psn
        where psn.start_date <= now()
          and (psn.next_date is null or psn.next_date <= now())
          and psn.status = 'ACTIVE'
        """, nativeQuery = true)
    List<PaymentScheduledNotification> findAllToPrepareNotification();

    @Query(value = """
        select distinct(psn), psn.* from payment_scheduled_notification psn
            join payment_receivers prc on prc.payment_scheduled_notification_id = psn.id
            where psn.user_id = :userId
            and psn.start_date >= cast(:startDate as date)
            and (cast(:endDate as date) is null or psn.end_date is not null and psn.end_date >= cast(:endDate as date))
            and (:title is null or psn.title ilike '%' || :title || '%')
            and (:receiver is null or prc.identification ilike '%' || :receiver || '%')
            and (cast(:nextDate as date) is null or psn.next_date is not null and psn.next_date >= cast(:nextDate as date))
            and (:status is null or psn.status = :status)
            and (:recurrence is null or psn.recurrence = :recurrence)
        """, nativeQuery = true)
    Slice<PaymentScheduledNotification> findPaymentNotificationsFiltering(
        @Param("userId") Long userId,
        @Param("title") String title,
        @Param("recurrence") String recurrence,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("nextDate") LocalDate nextDate,
        @Param("status") String status,
        @Param("receiver") String receiver,
        PageRequest pageRequest
    );
}
