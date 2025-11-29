package org.com.smartpayments.authenticator.integration.core.domain.usecase.user.authUser;

import org.com.smartpayments.authenticator.core.domain.enums.ERole;
import org.com.smartpayments.authenticator.core.domain.model.User;
import org.com.smartpayments.authenticator.core.ports.in.dto.AuthUserInput;
import org.com.smartpayments.authenticator.integration.fixtures.bases.IntegrationTestBase;
import org.com.smartpayments.authenticator.integration.utils.UserUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.com.smartpayments.authenticator.integration.constants.TestConstants.RANDOM_VALID_PASSWORD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthUserIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserUtils userUtils;

    @Value("${server.api-suffix}")
    private String apiSuffix;

    @Test
    @DisplayName("Should authenticate an existing user")
    public void authUser() throws Exception {
        User user = userUtils.createUser(ERole.MEMBER, true, true);

        AuthUserInput input = input(user.getEmail());

        mockMvc.perform(post(apiSuffix + "/user/auth").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input))
            ).andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andReturn();
    }

    @Test
    @DisplayName("Should verify e-mail format")
    public void authUserEmailFormat() throws Exception {
        AuthUserInput input = input("invalid_email");
        input.setEmail("invalid_email");

        mockMvc.perform(post(apiSuffix + "/user/auth").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input))
        ).andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Should verify password format")
    public void authUserPasswordFormat() throws Exception {
        User user = userUtils.createUser(ERole.MEMBER, true, true);
        AuthUserInput input = input(user.getEmail());
        input.setPassword("invalid_password");

        mockMvc.perform(post(apiSuffix + "/user/auth").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input))
            ).andExpect(status().is4xxClientError())
            .andExpect(jsonPath("$.message.password[0]").value("Password must have one special character, one upper letter and at least 6 characters!"));
    }

    @Test
    @DisplayName("Should not authenticate an existing user with wrong credentials")
    public void authUserWrongCredentials() throws Exception {
        User user = userUtils.createUser(ERole.MEMBER, true, true);

        AuthUserInput input = input(user.getEmail());
        input.setPassword("WrongPass#123");

        mockMvc.perform(post(apiSuffix + "/user/auth").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input))
            ).andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message").value("Wrong credentials!"));
    }


    private AuthUserInput input(String email) {
        return new AuthUserInput(email, RANDOM_VALID_PASSWORD);
    }
}
