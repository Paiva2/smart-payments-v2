package org.com.smartpayments.subscription.core.ports.in.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import static org.springframework.util.ObjectUtils.isEmpty;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentGatewayWebhookInput {
    private String id;
    private String event;
    private String dateCreated;
    private PaymentGatewayPaymentInput payment;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PaymentGatewayPaymentInput {
        private String id;
        private String customer;
        private String subscription;
        private BigDecimal value;
        private String description;
        private String billingType;
        private String confirmedDate;
        private String status;
        private String dueDate;
        private String paymentDate;
        private String clientPaymentDate;
        private String pixTransaction;
        private String pixQrCode;
        private String invoiceUrl;
        private String externalReference;
        private String transactionReceiptUrl;
        private String bankSlipUrl;
    }

    public boolean isFromSubscription() {
        return !isEmpty(this.payment.getSubscription());
    }
}
