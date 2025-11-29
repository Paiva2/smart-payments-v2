package org.com.smartpayments.authenticator.integration.core.domain.usecase.user.resetPasswordPublic;

import org.com.smartpayments.authenticator.core.domain.enums.ERole;
import org.com.smartpayments.authenticator.core.domain.model.User;
import org.com.smartpayments.authenticator.core.ports.in.dto.ResetPasswordInput;
import org.com.smartpayments.authenticator.core.ports.out.utils.PasswordUtilsPort;
import org.com.smartpayments.authenticator.infra.persistence.repository.UserRepository;
import org.com.smartpayments.authenticator.integration.fixtures.bases.IntegrationTestBase;
import org.com.smartpayments.authenticator.integration.utils.UserUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ResetPasswordPublicIntegrationTest extends IntegrationTestBase {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserUtils userUtils;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordUtilsPort passwordUtilsPort;

    @Value("${server.api-suffix}")
    private String apiSuffix;

    private User user;

    private String authToken;

    @BeforeEach
    public void beforeEach() {
        user = userUtils.createUser(ERole.MEMBER, true, true);
        user.setPasswordToken("random_token");
        user.setPasswordTokenSentAt(new Date());
        userRepository.save(user);

        authToken = "Bearer " + userUtils.generateAuthToken(user.getId());
    }

    @Test
    @DisplayName("Should reset password using token sent in e-mail")
    public void resetPassword() throws Exception {
        ResetPasswordInput input = input();

        mockMvc.perform(put(apiSuffix + "/user/reset_password/" + "random_token")
            .header("Authorization", authToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input))
        ).andExpect(status().isNoContent());

        Optional<User> userUpdated = userRepository.findById(user.getId());

        assertTrue(userUpdated.isPresent());
        assertNull(userUpdated.get().getPasswordToken());
        assertNotNull(userUpdated.get().getPasswordTokenSentAt());
        assertTrue(passwordUtilsPort.comparePassword(input.getPassword(), userUpdated.get().getPasswordHash()));
    }

    @Test
    @DisplayName("Should verify password format")
    public void resetPasswordPasswordFormat() throws Exception {
        ResetPasswordInput input = input();
        input.setPassword("invalid_password");

        mockMvc.perform(put(apiSuffix + "/user/reset_password/" + "random_token")
                .header("Authorization", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input))
            ).andExpect(status().is4xxClientError())
            .andExpect(jsonPath("$.message.password[0]").value("Password must have one special character, one upper letter and at least 6 characters!"));
    }

    private ResetPasswordInput input() {
        ResetPasswordInput input = new ResetPasswordInput();
        input.setPassword("MyNewPassword#123");
        return input;
    }
}
