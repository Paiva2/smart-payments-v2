package org.com.smartpayments.authenticator.integration.core.domain.usecase.user.changePassword;

import org.com.smartpayments.authenticator.core.domain.enums.ERole;
import org.com.smartpayments.authenticator.core.domain.model.User;
import org.com.smartpayments.authenticator.core.ports.in.dto.ChangePasswordInput;
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
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.com.smartpayments.authenticator.integration.constants.TestConstants.RANDOM_VALID_PASSWORD;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ChangePasswordIntegrationTest extends IntegrationTestBase {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserUtils userUtils;

    @Autowired
    private PasswordUtilsPort passwordUtilsPort;

    @Autowired
    private UserRepository userRepository;

    @MockitoSpyBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value("${spring.kafka.topics.mail-sender}")
    private String SEND_EMAIL_TOPIC;

    @Value("${server.api-suffix}")
    private String apiSuffix;

    private User user;

    private String authToken;

    @BeforeEach
    public void beforeEach() {
        user = userUtils.createUser(ERole.MEMBER, true, true);
        user.setPasswordToken(null);
        user.setPasswordTokenSentAt(null);
        userRepository.save(user);

        authToken = "Bearer " + userUtils.generateAuthToken(user.getId());
    }

    @Test
    @DisplayName("Should change user current password")
    public void changePassword() throws Exception {
        ChangePasswordInput input = input();

        mockMvc.perform(put(apiSuffix + "/user/change_password")
            .header("Authorization", authToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input))
        ).andExpect(status().isOk());

        Optional<User> userUpdated = userRepository.findById(user.getId());

        assertTrue(userUpdated.isPresent());
        assertFalse(passwordUtilsPort.comparePassword(RANDOM_VALID_PASSWORD, userUpdated.get().getPasswordHash()));
        assertTrue(passwordUtilsPort.comparePassword(input.getPassword(), userUpdated.get().getPasswordHash()));

        verify(kafkaTemplate, times(1)).send(eq(SEND_EMAIL_TOPIC), anyString());
    }

    @Test
    @DisplayName("Should verify new password format")
    public void changePasswordNewPasswordFormat() throws Exception {
        ChangePasswordInput input = input();
        input.setPassword("invalid_password");

        mockMvc.perform(put(apiSuffix + "/user/change_password")
            .header("Authorization", authToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input))
        ).andExpect(jsonPath("$.message.password[0]").value("Password must have one special character, one upper letter and at least 6 characters!"));
    }

    @Test
    @DisplayName("Should verify old password format")
    public void changePasswordOldPasswordFormat() throws Exception {
        ChangePasswordInput input = input();
        input.setOldPassword("invalid_password");

        mockMvc.perform(put(apiSuffix + "/user/change_password")
            .header("Authorization", authToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input))
        ).andExpect(jsonPath("$.message.oldPassword[0]").value("Password must have one special character, one upper letter and at least 6 characters!"));
    }

    private ChangePasswordInput input() {
        ChangePasswordInput input = new ChangePasswordInput();
        input.setPassword("MyNewPassword#123");
        input.setOldPassword(RANDOM_VALID_PASSWORD); // Used on UserUtils while creating User
        return input;
    }
}
