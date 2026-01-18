package org.com.smartpayments.subscription.core.ports.out.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetUserSubscriptionInternalOutput {
    private Long id;
    private String firstName;
    private String email;
    private String phoneNumber;
    private String plan;
    private Boolean unlimitedEmailCredits;
    private Boolean unlimitedSmsCredits;
    private Boolean unlimitedWhatsAppCredits;
}
