package org.com.smartpayments.authenticator.core.ports.in.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeEmailInput {
    // this field is set on controller
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long userId;

    @NotBlank
    @Email(regexp = "^([\\w-\\.]+){1,64}@([\\w&&[^_]]+){2,255}.[a-z]{2,}$")
    private String newEmail;
}
