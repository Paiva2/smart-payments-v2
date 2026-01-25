package com.smartpayments.scheduler.core.ports.in.usecase.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartpayments.scheduler.core.domain.enums.ENotificationRecurrence;
import com.smartpayments.scheduler.core.domain.enums.ENotificationScheduleStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListPaymentScheduledNotificationFilter {
    // this field is set on controller
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long userId;

    // this field is set on controller
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer page;

    // this field is set on controller
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer pageSize;

    // this field is set on controller
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Sort.Direction sortDirection;

    // this field is set on controller
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String sortBy;

    private String title;

    private ENotificationRecurrence recurrence;

    @NotNull
    @PastOrPresent
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate nextDate;

    private ENotificationScheduleStatus status;

    private String receiver;
}
