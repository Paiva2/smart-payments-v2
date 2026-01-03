package org.com.smartpayments.subscription.core.ports.in.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.com.smartpayments.subscription.core.domain.enums.EPaymentGatewayEvent;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseChargeOverdueInput {
    private String customerId;
    private boolean isFromSubscription;
    private String externalPurchaseId;
    private String externalChargeId;
    private Date dueDate;
    private EPaymentGatewayEvent event;
}
