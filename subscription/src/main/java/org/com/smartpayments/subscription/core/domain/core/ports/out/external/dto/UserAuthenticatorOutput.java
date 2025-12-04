package org.com.smartpayments.subscription.core.domain.core.ports.out.external.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAuthenticatorOutput {
    private Long id;
    private Boolean active;
    private String passwordHash;
    private List<String> roles;
}
