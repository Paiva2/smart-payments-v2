package org.com.smartpayments.subscription.core.ports.out.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.com.smartpayments.subscription.core.domain.enums.EPlan;
import org.com.smartpayments.subscription.core.domain.enums.ESubscriptionRecurrence;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsyncUserSubscriptionUpdateOutput {
    private Long userId;
    private EPlan plan;
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
