package org.com.smartpayments.subscription.core.ports.in.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.com.smartpayments.subscription.core.domain.enums.EPurchaseChargeStatus;
import org.com.smartpayments.subscription.core.domain.model.Purchase;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePurchaseChargeInput {
    // this field is set on usecases that call this one and already have Purchase
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Purchase purchase;

    private Long purchaseId;
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
