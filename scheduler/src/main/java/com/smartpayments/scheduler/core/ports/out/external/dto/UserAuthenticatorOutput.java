package com.smartpayments.scheduler.core.ports.out.external.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAuthenticatorOutput {
    private Long id;
    private Boolean active;
    private String passwordHash;
    private List<String> roles;
}