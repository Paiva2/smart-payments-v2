package org.com.smartpayments.subscription.core.ports.in.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.com.smartpayments.subscription.core.domain.enums.EPurchaseChargeStatus;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePurchaseChargeInput {
    private String externalPurchaseId;
    private String externalChargeId;
    private BigDecimal value;
    private String description;
    private String billingType;
    private String pixTransaction;
    private String pixQrCodeId;
    private EPurchaseChargeStatus status;
    private Date dueDate;
    private Date paymentDate;
    private String invoiceUrl;
    private String bankSlipUrl;
}
