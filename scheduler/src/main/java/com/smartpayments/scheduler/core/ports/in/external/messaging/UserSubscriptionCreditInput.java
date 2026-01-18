package com.smartpayments.scheduler.core.ports.in.external.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSubscriptionCreditInput {
    private Long userId;
    private String usageReason;
    private String usageReasonId;
    private int whatsAppCredits;
    private int smsCredits;
    private int emailCredits;
}
