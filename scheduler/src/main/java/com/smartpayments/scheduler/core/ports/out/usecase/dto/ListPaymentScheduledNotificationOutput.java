package com.smartpayments.scheduler.core.ports.out.usecase.dto;

import com.smartpayments.scheduler.core.domain.model.PaymentScheduledNotification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Slice;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListPaymentScheduledNotificationOutput {
    private Integer totalElements;
    private Integer page;
    private Integer pageSize;
    private List<PaymentScheduledNotificationOutput> content;

    public ListPaymentScheduledNotificationOutput(Slice<PaymentScheduledNotification> slice) {
        this.totalElements = slice.getNumberOfElements();
        this.page = slice.getNumber();
        this.pageSize = slice.getSize();
        this.content = slice.getContent().stream()
            .map(c -> PaymentScheduledNotificationOutput.builder()
                .id(c.getId())
                .title(c.getTitle())
                .description(c.getDescription())
                .value(c.getValue())
                .recurrence(c.getRecurrence())
                .startDate(c.getStartDate())
                .endDate(c.getEndDate())
                .nextDate(c.getNextDate())
                .lastDate(c.getLastDate())
                .lastExecutionStatus(c.getLastExecutionStatus())
                .status(c.getStatus())
                .notifyWhatsApp(c.getNotifyWhatsApp())
                .notifyEmail(c.getNotifyEmail())
                .notifySms(c.getNotifySms())
                .createdAt(c.getCreatedAt())
                .receivers(c.getReceivers().stream().map(rc -> PaymentReceiverOutput.builder()
                        .id(rc.getId())
                        .identification(rc.getIdentification())
                        .build()
                    ).toList()
                ).build())
            .toList();
    }
}
