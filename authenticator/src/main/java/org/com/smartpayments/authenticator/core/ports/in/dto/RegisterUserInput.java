package org.com.smartpayments.authenticator.core.ports.in.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
public class RegisterUserInput {
    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    @Email(regexp = "^([\\w-\\.]+){1,64}@([\\w&&[^_]]+){2,255}.[a-z]{2,}$")
    private String email;

    @NotBlank
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private String birthdate;

    @NotBlank
    @Size(min = 6)
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[^a-zA-Z0-9]).{6,}$", message = "Password must have one special character, one upper letter and at least 6 characters!")
    private String password;

    @NotBlank
    private String cpfCnpj;

    @NotNull
    private EUserType type;

    private String phone;

    @NotNull
    private Address address;

    @Data
    @Builder
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
