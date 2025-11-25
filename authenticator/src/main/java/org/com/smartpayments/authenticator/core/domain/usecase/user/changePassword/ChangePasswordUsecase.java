package org.com.smartpayments.authenticator.core.domain.usecase.user.changePassword;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.smartpayments.authenticator.core.common.exception.GenericException;
import org.com.smartpayments.authenticator.core.common.exception.GenericPasswordInvalidException;
import org.com.smartpayments.authenticator.core.common.exception.UserEmailNotActiveException;
import org.com.smartpayments.authenticator.core.common.exception.UserNotFoundException;
import org.com.smartpayments.authenticator.core.domain.model.User;
import org.com.smartpayments.authenticator.core.ports.in.UsecaseVoidPort;
import org.com.smartpayments.authenticator.core.ports.in.dto.ChangePasswordInput;
import org.com.smartpayments.authenticator.core.ports.out.dataProvider.AsyncMessageDataProviderPort;
import org.com.smartpayments.authenticator.core.ports.out.dataProvider.UserDataProviderPort;
import org.com.smartpayments.authenticator.core.ports.out.dto.AsyncEmailOutput;
import org.com.smartpayments.authenticator.core.ports.out.utils.PasswordUtilsPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;

import static java.util.Objects.isNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChangePasswordUsecase implements UsecaseVoidPort<ChangePasswordInput> {
    private final static ObjectMapper mapper = new ObjectMapper();
    private final static String MAIL_PASSWORD_CHANGED_TEMPLATE = "user-password-changed";
    private final static String MAIL_PASSWORD_CHANGED_SUBJECT = "Password changed";

    private final UserDataProviderPort userDataProviderPort;
    private final AsyncMessageDataProviderPort asyncMessageDataProviderPort;

    private final PasswordUtilsPort passwordUtilsPort;

    @Value("${spring.kafka.topics.mail-sender}")
    private String SEND_EMAIL_TOPIC;

    @Override
    public void execute(ChangePasswordInput input) {
        validatePassword(input.getPassword());

        User user = findUser(input.getUserId());

        if (isNull(user.getEmailConfirmedAt())) {
            throw new UserEmailNotActiveException();
        }

        checkOldPassword(user.getPasswordHash(), input.getOldPassword());
        user.setPasswordHash(passwordUtilsPort.hashPassword(input.getPassword()));

        persistUser(user);

        sendPasswordChangedEmail(user);
    }

    private User findUser(Long userId) {
        return userDataProviderPort.findActiveById(userId).orElseThrow(UserNotFoundException::new);
    }

    private void validatePassword(String password) {
        final int MAX_BYTES_PASSWORD = 72;

        if (password.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES_PASSWORD) {
            throw new GenericPasswordInvalidException("Password is too long!");
        }
    }

    private void checkOldPassword(String hashOldPassword, String rawOldPassword) {
        if (!passwordUtilsPort.comparePassword(rawOldPassword, hashOldPassword)) {
            throw new GenericPasswordInvalidException("Old password don't match!");
        }
    }

    private void persistUser(User user) {
        userDataProviderPort.persist(user);
    }

    private void sendPasswordChangedEmail(User user) {
        final AsyncEmailOutput email = AsyncEmailOutput.builder()
            .to(user.getEmail())
            .templateName(MAIL_PASSWORD_CHANGED_TEMPLATE)
            .subject(MAIL_PASSWORD_CHANGED_SUBJECT)
            .cc(new ArrayList<>())
            .variables(Collections.singletonMap("${FIRST_NAME}", user.getFirstName()))
            .build();

        try {
            asyncMessageDataProviderPort.sendMessage(SEND_EMAIL_TOPIC, mapper.writeValueAsString(email));
        } catch (JsonProcessingException e) {
            String message = "Error while sending email!";
            log.error(message, e);
            throw new GenericException(message);
        }
    }
}
