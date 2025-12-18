package org.com.smartpayments.subscription.core.ports.out.dto;

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