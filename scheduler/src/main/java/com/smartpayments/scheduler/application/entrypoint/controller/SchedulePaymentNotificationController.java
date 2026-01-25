package com.smartpayments.scheduler.application.entrypoint.controller;

import com.smartpayments.scheduler.core.domain.usecase.paymentScheduledNotification.cancelPaymentScheduledNotification.CancelPaymentScheduledNotificationUsecase;
import com.smartpayments.scheduler.core.domain.usecase.paymentScheduledNotification.listPaymentScheduledNotificatios.ListPaymentScheduledNotificationsUsecase;
import com.smartpayments.scheduler.core.domain.usecase.paymentScheduledNotification.schedulePaymentNotification.SchedulePaymentNotificationUsecase;
import com.smartpayments.scheduler.core.ports.in.usecase.dto.CancelPaymentScheduledNotificationInput;
import com.smartpayments.scheduler.core.ports.in.usecase.dto.ListPaymentScheduledNotificationFilter;
import com.smartpayments.scheduler.core.ports.in.usecase.dto.SchedulePaymentNotificationInput;
import com.smartpayments.scheduler.core.ports.out.usecase.dto.ListPaymentScheduledNotificationOutput;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("${server.api-suffix}")
public class SchedulePaymentNotificationController {
    private final SchedulePaymentNotificationUsecase schedulePaymentNotificationUsecase;
    private final ListPaymentScheduledNotificationsUsecase listPaymentScheduledNotificationsUsecase;
    private final CancelPaymentScheduledNotificationUsecase cancelPaymentScheduledNotificationUsecase;

    @PostMapping("/payment-notification")
    public ResponseEntity<Void> create(Authentication authentication, @RequestBody @Valid SchedulePaymentNotificationInput input) {
        Long userId = (Long) authentication.getPrincipal();
        input.setUserId(userId);
        schedulePaymentNotificationUsecase.execute(input);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/payment-notification/list")
    public ResponseEntity<ListPaymentScheduledNotificationOutput> getFiltering(
        Authentication authentication,
        @RequestParam(required = false, defaultValue = "0") int page,
        @RequestParam(required = false, defaultValue = "5") int pageSize,
        @RequestParam(required = false, defaultValue = "desc") String sortDirection,
        @RequestParam(required = false, defaultValue = "created_at") String sortBy,
        @RequestBody @Valid ListPaymentScheduledNotificationFilter input
    ) {
        Long userId = (Long) authentication.getPrincipal();
        input.setUserId(userId);
        input.setPage(page);
        input.setPageSize(pageSize);
        input.setSortDirection(Sort.Direction.fromString(sortDirection));
        input.setSortBy(sortBy);
        ListPaymentScheduledNotificationOutput output = listPaymentScheduledNotificationsUsecase.execute(input);
        return ResponseEntity.status(HttpStatus.OK).body(output);
    }

    @PutMapping("/payment-notification/cancel")
    public ResponseEntity<Void> cancel(Authentication authentication, @RequestBody @Valid CancelPaymentScheduledNotificationInput input) {
        Long userId = (Long) authentication.getPrincipal();
        input.setUserId(userId);
        cancelPaymentScheduledNotificationUsecase.execute(input);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
