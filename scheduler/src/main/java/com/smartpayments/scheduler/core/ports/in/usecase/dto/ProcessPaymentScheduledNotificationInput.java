package com.smartpayments.scheduler.core.ports.in.usecase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessPaymentScheduledNotificationInput {
    private String messageHash;
    private Long id;
}
