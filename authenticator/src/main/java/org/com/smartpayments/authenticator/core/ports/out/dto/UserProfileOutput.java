package org.com.smartpayments.authenticator.core.ports.out.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.com.smartpayments.authenticator.core.domain.enums.EUserType;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileOutput {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String cpfCnpj;
    private EUserType type;
    private String ddi;
    private String phone;
    private Date birthdate;
    private Boolean active;
    private Date createdAt;
    private Date updatedAt;
    private Date emailConfirmedAt;
    private AddressOutput address;
    private List<RoleOutput> roles;
}
