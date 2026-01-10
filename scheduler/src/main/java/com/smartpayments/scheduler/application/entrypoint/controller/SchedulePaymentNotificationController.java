package com.smartpayments.scheduler.application.entrypoint.controller;

import com.smartpayments.scheduler.core.domain.usecase.paymentScheduledNotification.schedulePaymentNotification.SchedulePaymentNotificationUsecase;
import com.smartpayments.scheduler.core.ports.in.usecase.dto.SchedulePaymentNotificationInput;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("${server.api-suffix}")
public class SchedulePaymentNotificationController {
    private final SchedulePaymentNotificationUsecase schedulePaymentNotificationUsecase;

    @PostMapping("/payment-notification")
    public ResponseEntity<Void> create(/*Authentication authentication, */@RequestBody @Valid SchedulePaymentNotificationInput input) {
        input.setUserId(1L);
        schedulePaymentNotificationUsecase.execute(input);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
