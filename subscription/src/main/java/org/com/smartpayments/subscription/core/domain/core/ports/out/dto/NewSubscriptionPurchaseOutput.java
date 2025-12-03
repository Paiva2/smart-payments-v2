package org.com.smartpayments.subscription.core.domain.core.ports.out.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewSubscriptionPurchaseOutput {
    private String paymentUrl;
}
