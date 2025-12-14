package org.com.smartpayments.authenticator.core.ports.out.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserSubscriptionOutput {
    private BigDecimal value;
    private Date nextPaymentDate;
    private ESubscriptionStatus status;
    private ESubscriptionRecurrence recurrence;
    private EPlan plan;
    private Boolean unlimitedEmailCredits;
    private Integer emailCredits;
    private Boolean unlimitedWhatsAppCredits;
    private Integer whatsAppCredits;
    private Boolean unlimitedSmsCredits;
    private Integer smsCredits;
}
