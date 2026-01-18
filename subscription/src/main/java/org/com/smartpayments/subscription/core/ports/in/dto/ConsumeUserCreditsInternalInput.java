package org.com.smartpayments.subscription.core.ports.in.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsumeUserCreditsInternalInput {
    // this field is set on the controller
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String idempotencyKey;

    private Long userId;
    private String usageReason;
    private String usageReasonId;
    private int whatsAppCredits;
    private int smsCredits;
    private int emailCredits;
}
