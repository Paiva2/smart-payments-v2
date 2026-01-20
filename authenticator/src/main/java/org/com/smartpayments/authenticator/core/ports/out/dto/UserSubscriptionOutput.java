package org.com.smartpayments.authenticator.core.ports.out.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.com.smartpayments.authenticator.core.domain.enums.EPlan;
import org.com.smartpayments.authenticator.core.domain.enums.ESubscriptionRecurrence;
import org.com.smartpayments.authenticator.core.domain.enums.ESubscriptionStatus;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSubscriptionOutput {
    private BigDecimal value;
    private Date nextPaymentDate;
    private ESubscriptionStatus status;
    private ESubscriptionRecurrence recurrence;
    private EPlan plan;
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
