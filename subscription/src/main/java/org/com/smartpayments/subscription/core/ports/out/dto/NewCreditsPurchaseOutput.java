package org.com.smartpayments.subscription.core.ports.out.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewCreditsPurchaseOutput {
    private String paymentUrl;
}
