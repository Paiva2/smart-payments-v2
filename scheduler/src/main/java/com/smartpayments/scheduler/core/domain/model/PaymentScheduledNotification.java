package com.smartpayments.scheduler.core.domain.model;

import com.smartpayments.scheduler.core.domain.enums.ENotificationExecutionStatus;
import com.smartpayments.scheduler.core.domain.enums.ENotificationRecurrence;
import com.smartpayments.scheduler.core.domain.enums.ENotificationScheduleStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payment_scheduled_notification")
public class PaymentScheduledNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "title", nullable = false, unique = true)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "value", nullable = false)
    private BigDecimal value;

    @Enumerated(EnumType.STRING)
    @Column(name = "recurrence", nullable = false)
    private ENotificationRecurrence recurrence;

    @Column(name = "start_date")
    private Date startDate;

    @Column(name = "end_date")
    private Date endDate;

    @Column(name = "next_date")
    private Date nextDate;

    @Column(name = "last_date")
    private Date lastDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "last_execution_status")
    private ENotificationExecutionStatus lastExecutionStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ENotificationScheduleStatus status;

    @Column(name = "notify_whatsapp", nullable = false)
    private Boolean notifyWhatsApp;

    @Column(name = "notify_email", nullable = false)
    private Boolean notifyEmail;

    @Column(name = "notify_sms", nullable = false)
    private Boolean notifySms;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Date updatedAt;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "paymentScheduledNotification")
    private List<PaymentReceiver> receivers;
}
