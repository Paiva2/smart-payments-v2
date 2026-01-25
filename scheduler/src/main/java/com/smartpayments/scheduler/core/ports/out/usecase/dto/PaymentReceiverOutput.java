package com.smartpayments.scheduler.core.ports.out.usecase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentReceiverOutput {
    private Long id;
    private String identification;
}
