package org.com.smartpayments.authenticator.core.ports.in.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.com.smartpayments.authenticator.core.domain.enums.EBrState;
import org.com.smartpayments.authenticator.core.domain.enums.EUserType;
import org.springframework.format.annotation.DateTimeFormat;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserProfileInput {
    // this field is set on controller
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long userId;

    private String firstName;

    private String lastName;

    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private String birthdate;

    private String cpfCnpj;

    private EUserType type;

    private String phone;

    private Address address;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Address {
        private String street;

        private String neighborhood;

        private String number;

        private String zipcode;

        private String city;

        @Size(min = 2, max = 2)
        private EBrState state;

        private String complement;
    }
}
