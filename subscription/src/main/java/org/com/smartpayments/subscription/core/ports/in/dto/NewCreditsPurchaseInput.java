package org.com.smartpayments.subscription.core.ports.in.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
public class NewCreditsPurchaseInput {
    // this field is set on controller
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long userId;

    @NotNull
    private EPaymentMethod paymentMethod;

    @Valid
    @NotNull
    @NotEmpty
    private List<PurchaseItemInput> purchaseItems;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PurchaseItemInput {
        @NotNull
        @Min(value = 1)
        private Integer quantity;

        @NotNull
        private Long creditId;
    }
}
