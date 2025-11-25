package org.com.smartpayments.authenticator.core.ports.in.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordInput {
    // this field is set on controller
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long userId;

    @NotBlank
    @Size(min = 6)
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[^a-zA-Z0-9]).{6,}$", message = "Password must have one special character, one upper letter and at least 6 characters!")
    private String password;

    @NotBlank
    @Size(min = 6)
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[^a-zA-Z0-9]).{6,}$", message = "Password must have one special character, one upper letter and at least 6 characters!")
    private String oldPassword;
}
