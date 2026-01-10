package com.smartpayments.scheduler.core.ports.in.usecase.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartpayments.scheduler.core.domain.enums.ENotificationRecurrence;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchedulePaymentNotificationInput {
    // this field is set on controller
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long userId;

    @NotBlank(message = "Title can't be empty")
    private String title;

    @Size(max = 500, message = "Description can't have more than 500 characters")
    private String description;

    @DecimalMin(value = "1", message = "Value can't be less than 1")
    @NotNull(message = "Value can't be empty")
    private BigDecimal value;

    @NotNull(message = "Recurrence can't be empty")
    private ENotificationRecurrence recurrence;

    @NotNull(message = "Start date can't be empty")
    private Date startDate;

    private Date endDate;

    @NotNull(message = "Notify WhatsApp can't be empty")
    private Boolean notifyWhatsApp;

    @NotNull(message = "Notify E-mail can't be empty")
    private Boolean notifyEmail;

    @NotNull(message = "Notify SMS can't be empty")
    private Boolean notifySms;

    @Valid
    @NotEmpty(message = "Receivers list can't be empty")
    private List<ReceiverInput> receivers;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReceiverInput {
        @NotBlank(message = "Receiver identification can't be empty")
        private String identification;
    }
}
