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
public class NewSubscriptionClientInput {
    private String customer;
    private String billingType;
    private BigDecimal value;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date nextDueDate;
    private final String cycle = "MONTHLY";
    private String description;
    private Date endDate;
    private Integer maxPayments;
    private String externalReference;
}
