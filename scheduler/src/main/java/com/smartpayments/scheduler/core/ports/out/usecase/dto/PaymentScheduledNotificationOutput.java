package com.smartpayments.scheduler.core.ports.out.usecase.dto;

import com.smartpayments.scheduler.core.domain.enums.ENotificationExecutionStatus;
import com.smartpayments.scheduler.core.domain.enums.ENotificationRecurrence;
import com.smartpayments.scheduler.core.domain.enums.ENotificationScheduleStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentScheduledNotificationOutput {
    private Long id;
    private String title;
    private String description;
    private BigDecimal value;
    private ENotificationRecurrence recurrence;
    private Date startDate;
    private Date endDate;
    private Date nextDate;
    private Date lastDate;
    private ENotificationExecutionStatus lastExecutionStatus;
    private ENotificationScheduleStatus status;
    private Boolean notifyWhatsApp;
    private Boolean notifyEmail;
    private Boolean notifySms;
    private Date createdAt;
    private List<PaymentReceiverOutput> receivers;
}
