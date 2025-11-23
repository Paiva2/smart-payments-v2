package org.com.smartpayments.authenticator.core.domain.usecase.user.forgotPassword;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.smartpayments.authenticator.core.common.exception.GenericException;
import org.com.smartpayments.authenticator.core.common.exception.UserNotFoundException;
import org.com.smartpayments.authenticator.core.domain.model.User;
import org.com.smartpayments.authenticator.core.ports.in.UsecasePort;
import org.com.smartpayments.authenticator.core.ports.in.dto.ForgotPasswordInput;
import org.com.smartpayments.authenticator.core.ports.out.dataProvider.AsyncMessageDataProviderPort;
import org.com.smartpayments.authenticator.core.ports.out.dataProvider.UserDataProviderPort;
import org.com.smartpayments.authenticator.core.ports.out.dto.AsyncEmailOutput;
import org.com.smartpayments.authenticator.core.ports.out.dto.ForgotPasswordOutput;
import org.com.smartpayments.authenticator.core.ports.out.utils.TokenUtilsPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import static java.util.Objects.isNull;
import static org.com.smartpayments.authenticator.core.common.constants.Constants.MAX_EXPIRATION_DAYS_PASSWORD_TOKEN;
import static org.com.smartpayments.authenticator.core.common.constants.Constants.MINIMUM_LIMIT_SEND_FORGOT_PASSWORD_MINUTES;

@Slf4j
@Service
@RequiredArgsConstructor
public class ForgotPasswordUsecase implements UsecasePort<ForgotPasswordInput, ForgotPasswordOutput> {
    private final static ObjectMapper mapper = new ObjectMapper();
    private final static String MAIL_TEMPLATE = "user-forgot-password-email";
    private final static String MAIL_SUBJECT = "Forgot password";

    private final UserDataProviderPort userDataProviderPort;
    private final AsyncMessageDataProviderPort asyncMessageDataProviderPort;

    private final TokenUtilsPort tokenUtilsPort;

    @Value("${spring.kafka.topics.mail-sender}")
    private String SEND_EMAIL_TOPIC;

    @Value("${ui.url}")
    private String UI_URL;

    @Override
    @Transactional
    public ForgotPasswordOutput execute(ForgotPasswordInput input) {
        User user = findUser(input.getEmail());

        checkLastSentAt(user.getPasswordTokenSentAt());

        user.setPasswordTokenSentAt(new Date());
        user.setPasswordToken(generatePasswordToken());
        user = persistUser(user);

        sendEmailForgotPassword(user);

        return new ForgotPasswordOutput(user.getPasswordToken());
    }

    private User findUser(String email) {
        return userDataProviderPort.findActiveByEmail(email).orElseThrow(UserNotFoundException::new);
    }

    private void checkLastSentAt(Date lastSentAt) {
        if (isNull(lastSentAt)) return;

        Calendar limit = Calendar.getInstance();
        limit.add(Calendar.MINUTE, -MINIMUM_LIMIT_SEND_FORGOT_PASSWORD_MINUTES);

        if (lastSentAt.after(limit.getTime())) {
            throw new GenericException("You must wait at least " + MINIMUM_LIMIT_SEND_FORGOT_PASSWORD_MINUTES + " minutes to resend the password reset e-mail!");
        }
    }

    private String generatePasswordToken() {
        final int MAX_GENERATED_TOKEN_ATTEMPTS = 10;
        final int TOKEN_BYTES = 32;

        for (int i = 0; i < MAX_GENERATED_TOKEN_ATTEMPTS; i++) {
            String randomToken = tokenUtilsPort.generateUrlBasedToken(TOKEN_BYTES);

            if (userDataProviderPort.findActiveByPasswordToken(randomToken).isEmpty()) {
                return randomToken;
            }
        }

        throw new GenericException("Error generating password token!");
    }

    private User persistUser(User user) {
        return userDataProviderPort.persist(user);
    }

    private String mountActivationLink(String emailToken) {
        return UI_URL + "/user/reset_password/" + emailToken;
    }

    private HashMap<String, Object> fillEmailVariables(User user) {
        return new HashMap<>() {{
            put("${FIRST_NAME}", user.getFirstName());
            put("${RESET_LINK}", mountActivationLink(user.getEmailToken()));
            put("${EXPIRATION_TIME}", MAX_EXPIRATION_DAYS_PASSWORD_TOKEN);
        }};
    }

    private void sendEmailForgotPassword(User user) {
        final AsyncEmailOutput email = AsyncEmailOutput.builder()
            .to(user.getEmail())
            .templateName(MAIL_TEMPLATE)
            .subject(MAIL_SUBJECT)
            .cc(new ArrayList<>())
            .variables(fillEmailVariables(user))
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
