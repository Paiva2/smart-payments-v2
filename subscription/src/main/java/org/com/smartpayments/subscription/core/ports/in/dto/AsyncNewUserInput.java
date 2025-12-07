package org.com.smartpayments.subscription.core.ports.in.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.com.smartpayments.subscription.core.domain.enums.EBrState;
import org.com.smartpayments.subscription.core.domain.enums.ECountry;
import org.com.smartpayments.subscription.core.domain.enums.EUserType;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsyncNewUserInput {
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
    private Date emailConfirmedAt;
    private Date createdAt;
    private Date updatedAt;

    private AsyncAddressOutput address;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AsyncAddressOutput {
        private String street;
        private String neighborhood;
        private String number;
        private String zipcode;
        private String complement;
        private String city;
        private EBrState state;
        private ECountry country;
    }
}