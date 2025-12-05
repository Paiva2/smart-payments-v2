package org.com.smartpayments.subscription.core.ports.out.external.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewCreditChargeClientInput {
    private String customer;
    private String billingType;
    private BigDecimal value;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date dueDate;
    private String description;
    private final int daysAfterDueDateToRegistrationCancellation = 0;
    private String externalReference;
    private final int installmentCount = 0;
}
