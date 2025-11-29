package org.com.smartpayments.authenticator.integration.core.domain.usecase.user.forgotPassword;

import org.com.smartpayments.authenticator.core.domain.enums.ERole;
import org.com.smartpayments.authenticator.core.domain.model.User;
import org.com.smartpayments.authenticator.core.ports.in.dto.ForgotPasswordInput;
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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ForgotPasswordIntegrationTest extends IntegrationTestBase {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserUtils userUtils;

    @Autowired
    private UserRepository userRepository;

    @MockitoSpyBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value("${server.api-suffix}")
    private String apiSuffix;

    @Value("${spring.kafka.topics.mail-sender}")
    private String SEND_EMAIL_TOPIC;

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
    @DisplayName("Should send e-mail to update e-mail")
    public void forgotPassword() throws Exception {
        ForgotPasswordInput input = input();

        mockMvc.perform(put(apiSuffix + "/user/forgot_password")
            .header("Authorization", authToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input))
        ).andExpect(status().isOk());

        Optional<User> userUpdated = userRepository.findById(user.getId());

        assertTrue(userUpdated.isPresent());
        assertNotNull(userUpdated.get().getPasswordToken());
        assertNotNull(userUpdated.get().getPasswordTokenSentAt());

        verify(kafkaTemplate, times(1)).send(eq(SEND_EMAIL_TOPIC), anyString());
    }

    private ForgotPasswordInput input() {
        return new ForgotPasswordInput(user.getEmail());
    }
}
