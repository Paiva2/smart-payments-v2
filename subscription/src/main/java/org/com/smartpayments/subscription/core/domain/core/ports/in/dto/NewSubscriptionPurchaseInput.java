package org.com.smartpayments.subscription.core.domain.core.ports.in.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.com.smartpayments.subscription.core.domain.enums.EPaymentMethod;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewSubscriptionPurchaseInput {
    // this field is set on controller
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long userId;

    @NotNull
    private EPaymentMethod paymentMethod;

    @NotNull
    @PositiveOrZero
    private Integer installments; // todo colocar no plan/credit se pode ter parcela e quantas

    @Valid
    @NotNull
    @NotEmpty
    private List<PurchaseItemInput> purchaseItems;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PurchaseItemInput {
        private Long planId;
    }
}
