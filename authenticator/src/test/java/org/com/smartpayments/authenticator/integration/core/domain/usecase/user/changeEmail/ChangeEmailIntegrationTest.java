package org.com.smartpayments.authenticator.integration.core.domain.usecase.user.changeEmail;

import org.com.smartpayments.authenticator.core.domain.enums.ERole;
import org.com.smartpayments.authenticator.core.domain.model.User;
import org.com.smartpayments.authenticator.core.ports.in.dto.ChangeEmailInput;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ChangeEmailIntegrationTest extends IntegrationTestBase {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserUtils userUtils;

    @Autowired
    private UserRepository userRepository;

    @Value("${server.api-suffix}")
    private String apiSuffix;

    private User user;

    private String authToken;

    @BeforeEach
    public void beforeEach() {
        user = userUtils.createUser(ERole.MEMBER, true, true);
        user.setEmailTokenSentAt(null);
        userRepository.save(user);

        authToken = "Bearer " + userUtils.generateAuthToken(user.getId());
    }

    @Test
    @DisplayName("Should change user email and make e-mail not confirmed")
    public void changeEmail() throws Exception {
        ChangeEmailInput input = input();

        mockMvc.perform(put(apiSuffix + "/user/change_email")
            .header("Authorization", authToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input))
        ).andExpect(status().isNoContent());

        Optional<User> userUpdated = userRepository.findById(user.getId());

        assertTrue(userUpdated.isPresent());
        assertNull(userUpdated.get().getEmailConfirmedAt());
        assertNotNull(userUpdated.get().getEmailTokenSentAt());
        assertEquals(input.getNewEmail(), userUpdated.get().getEmail());
    }

    @Test
    @DisplayName("Should verify e-mail format")
    public void changeEmailEmailFormat() throws Exception {
        ChangeEmailInput input = input();
        input.setNewEmail("invalid_email");

        mockMvc.perform(put(apiSuffix + "/user/change_email")
            .header("Authorization", authToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input))
        ).andExpect(status().is4xxClientError());
    }

    private ChangeEmailInput input() {
        ChangeEmailInput input = new ChangeEmailInput();
        input.setNewEmail("new_email@gmail.com");
        return input;
    }
}
