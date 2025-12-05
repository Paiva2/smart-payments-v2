package org.com.smartpayments.subscription.core.ports.out.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetSubscriptionChargesOutput {
    private List<DataOutput> data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DataOutput {
        private String id;
        private Date dateCreated;
        private String subscription;
        private String invoiceUrl;
        private String description;
        private Date paymentDate;
        private Date dueDate;
        private String bankSlipUrl;
        private BigDecimal value;
        private String billingType;
        private String pixTransaction;
        private String pixQrCodeId;
        private String status;
        private String externalReference;
    }
}
