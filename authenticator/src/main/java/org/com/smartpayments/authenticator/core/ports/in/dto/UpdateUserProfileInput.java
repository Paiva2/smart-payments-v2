package org.com.smartpayments.authenticator.core.ports.in.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.com.smartpayments.authenticator.core.domain.enums.EBrState;
import org.com.smartpayments.authenticator.core.domain.enums.EUserType;
import org.springframework.format.annotation.DateTimeFormat;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserProfileInput {
    // this field is set on controller
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long userId;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private String birthdate;

    @NotBlank
    private String cpfCnpj;

    @NotNull
    private EUserType type;

    private String phone;

    @NotNull
    private Address address;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Address {
        @NotBlank
        private String street;

        @NotBlank
        private String neighborhood;

        @NotBlank
        private String number;

        @NotBlank
        private String zipcode;

        @NotBlank
        private String city;

        @NotBlank
        @Size(min = 2, max = 2)
        private EBrState state;

        private String complement;
    }
}
