package com.smartpayments.scheduler.core.ports.out.external.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsumeUserSubscriptionCreditsOutput {
    private Long userId;
    private String sms;
    private String email;
    private String whatsapp;
}
