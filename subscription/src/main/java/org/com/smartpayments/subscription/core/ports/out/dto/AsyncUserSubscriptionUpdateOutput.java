package org.com.smartpayments.subscription.core.ports.out.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.com.smartpayments.subscription.core.domain.enums.EPlan;
import org.com.smartpayments.subscription.core.domain.enums.ESubscriptionRecurrence;
import org.com.smartpayments.subscription.core.domain.enums.ESubscriptionStatus;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsyncUserSubscriptionUpdateOutput {
    private Long userId;
    private EPlan plan;
    private ESubscriptionStatus status;
    private String nextPaymentDate;
    private ESubscriptionRecurrence recurrence;
    private BigDecimal value;
    private Integer emailCredits;
    private Integer whatsAppCredits;
    private Integer smsCredits;
    private Integer subscriptionEmailCredits;
    private Integer subscriptionSmsCredits;
    private Integer subscriptionWhatsAppCredits;
    private Boolean unlimitedEmailCredits;
    private Boolean unlimitedSmsCredits;
    private Boolean unlimitedWhatsAppCredits;
}
