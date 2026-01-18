package com.smartpayments.scheduler.core.ports.out.external.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSubscriptionOutput {
    private Long id;
    private String firstName;
    private String email;
    private String phoneNumber;
    private String plan;
    private Boolean unlimitedEmailCredits;
    private Boolean unlimitedSmsCredits;
    private Boolean unlimitedWhatsAppCredits;
}
