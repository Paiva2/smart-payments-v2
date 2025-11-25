package org.com.smartpayments.authenticator.core.ports.out.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.com.smartpayments.authenticator.core.domain.enums.ERole;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleOutput implements Serializable {
    private Long id;
    private ERole name;
}
