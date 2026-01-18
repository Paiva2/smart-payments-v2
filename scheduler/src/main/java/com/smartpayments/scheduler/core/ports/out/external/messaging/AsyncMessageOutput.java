package com.smartpayments.scheduler.core.ports.out.external.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsyncMessageOutput<D> {
    private String messageHash;
    private Date timestamp;
    private String issuer;
    private D data;
}