package org.com.smartpayments.authenticator.integration.core.domain.usecase.user.activeEmail;

import org.com.smartpayments.authenticator.core.domain.enums.ERole;
import org.com.smartpayments.authenticator.core.domain.model.User;
import org.com.smartpayments.authenticator.infra.persistence.repository.UserRepository;
import org.com.smartpayments.authenticator.integration.fixtures.bases.IntegrationTestBase;
import org.com.smartpayments.authenticator.integration.utils.UserUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Calendar;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ActiveEmailIntegrationTest extends IntegrationTestBase {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserUtils userUtils;

    @Autowired
    private UserRepository userRepository;

    @Value("${server.api-suffix}")
    private String apiSuffix;

    @Test
    @DisplayName("Should activate an e-mail from token")
    public void activateUserEmail() throws Exception {
        User user = userUtils.createUser(ERole.MEMBER, true, false);

        mockMvc.perform(put(apiSuffix + "/user/email_activation/" + user.getEmailToken()))
            .andExpect(status().isOk())
            .andExpect(content().string("E-mail activated. You can close this window."));

        Optional<User> userOptional = userRepository.findById(user.getId());

        assertTrue(userOptional.isPresent());
        assertNotNull(userOptional.get().getEmailConfirmedAt());
    }

    @Test
    @DisplayName("Should not activate an e-mail from token if token expired")
    public void activateUserEmailTokenLimit() throws Exception {
        User user = userUtils.createUser(ERole.MEMBER, true, false);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -5);

        user.setEmailTokenSentAt(calendar.getTime());

        userRepository.save(user);

        mockMvc.perform(put(apiSuffix + "/user/email_activation/" + user.getEmailToken()))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Invalid link. This link is invalid or has been already used."));
    }

    @Test
    @DisplayName("Should not activate an e-mail from token if user e-mail is already active")
    public void activateUserEmailTokenAlreadyActive() throws Exception {
        User user = userUtils.createUser(ERole.MEMBER, true, true);

        mockMvc.perform(put(apiSuffix + "/user/email_activation/" + user.getEmailToken()))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Invalid link. This link is invalid or has been already used."));
    }
}
