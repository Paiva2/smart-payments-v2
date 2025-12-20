package org.com.smartpayments.authenticator.core.ports.in.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.com.smartpayments.authenticator.core.domain.enums.EPlan;
import org.com.smartpayments.authenticator.core.domain.enums.ESubscriptionRecurrence;
import org.com.smartpayments.authenticator.core.domain.enums.ESubscriptionStatus;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSubscriptionUpdateInput {
    private Long userId;
    private EPlan plan;
    private ESubscriptionStatus status;
    private String nextPaymentDate;
    private ESubscriptionRecurrence recurrence;
    private BigDecimal value;
    private Boolean unlimitedEmailCredits;
    private Integer emailCredits;
    private Boolean unlimitedWhatsAppCredits;
    private Integer whatsAppCredits;
    private Boolean unlimitedSmsCredits;
    private Integer smsCredits;
}