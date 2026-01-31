package com.smartpayments.scheduler.core.ports.in.usecase.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartpayments.scheduler.core.domain.enums.ENotificationRecurrence;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EditPaymentScheduledNotificationInput {
    // this field is set on controller
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long userId;

    // this field is set on controller
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    private String title;

    @Size(max = 500, message = "Description can't have more than 500 characters")
    private String description;

    @DecimalMin(value = "1", message = "Value can't be less than 1")
    private BigDecimal value;

    private ENotificationRecurrence recurrence;

    private Date startDate;

    private Date endDate;

    private Boolean notifyWhatsApp;

    private Boolean notifyEmail;

    private Boolean notifySms;

    @Valid
    @Size(min = 1, message = "Receivers list can't be empty")
    private List<ReceiverInput> receivers;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReceiverInput {
        @NotBlank(message = "Receiver identification can't be empty")
        private String identification;
    }
}
