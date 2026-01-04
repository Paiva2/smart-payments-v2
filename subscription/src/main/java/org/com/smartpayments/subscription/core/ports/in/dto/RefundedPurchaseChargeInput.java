package org.com.smartpayments.subscription.core.ports.in.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.com.smartpayments.subscription.core.domain.enums.EPaymentGatewayEvent;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundedPurchaseChargeInput {
    private String customerId;
    private boolean isFromSubscription;
    private String externalPurchaseId;
    private String externalChargeId;
    private EPaymentGatewayEvent event;
}
