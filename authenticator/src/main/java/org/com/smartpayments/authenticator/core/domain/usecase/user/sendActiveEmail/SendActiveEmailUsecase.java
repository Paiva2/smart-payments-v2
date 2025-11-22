package org.com.smartpayments.authenticator.core.domain.usecase.user.sendActiveEmail;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.smartpayments.authenticator.core.common.exception.GenericException;
import org.com.smartpayments.authenticator.core.common.exception.UserNotActiveException;
import org.com.smartpayments.authenticator.core.common.exception.UserNotFoundException;
import org.com.smartpayments.authenticator.core.domain.model.User;
import org.com.smartpayments.authenticator.core.domain.usecase.user.activeEmail.exception.UserEmailAlreadyActiveException;
import org.com.smartpayments.authenticator.core.ports.in.UsecaseVoidPort;
import org.com.smartpayments.authenticator.core.ports.in.dto.SendActiveEmailInput;
import org.com.smartpayments.authenticator.core.ports.out.dataProvider.AsyncMessageDataProviderPort;
import org.com.smartpayments.authenticator.core.ports.out.dataProvider.UserDataProviderPort;
import org.com.smartpayments.authenticator.core.ports.out.dto.AsyncEmailOutput;
import org.com.smartpayments.authenticator.core.ports.out.utils.TokenUtilsPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class SendActiveEmailUsecase implements UsecaseVoidPort<SendActiveEmailInput> {
    private final static ObjectMapper mapper = new ObjectMapper();
    private final static Integer LINK_VALIDATION_IN_DAYS = 1;
    private final static Integer MINIMUM_LIMIT_RESEND_EMAIL_MINUTES = 5;
    private final static String MAIL_ACTIVATION_TEMPLATE = "user-email-activation-resend";
    private final static String MAIL_ACTIVATION_SUBJECT = "Confirm your e-mail";

    private final UserDataProviderPort userDataProviderPort;
    private final AsyncMessageDataProviderPort asyncMessageDataProviderPort;

    private final TokenUtilsPort tokenUtilsPort;

    @Value("${spring.kafka.topics.mail-sender}")
    private String SEND_EMAIL_TOPIC;

    @Value("${kong.url}")
    private String GATEWAY_URL;

    @Value("${server.api-suffix}")
    private String API_SUFFIX;

    @Override
    public void execute(SendActiveEmailInput input) {
        User user = findUser(input.getEmail());

        if (!user.getActive()) {
            throw new UserNotActiveException();
        }

        if (nonNull(user.getEmailConfirmedAt())) {
            throw new UserEmailAlreadyActiveException();
        }

        checkLastSentAt(user.getEmailTokenSentAt());

        user.setEmailTokenSentAt(new Date());
        user.setEmailToken(generateEmailToken());
        user = persistUser(user);

        sendEmailActivationEmail(user);
    }

    private User findUser(String email) {
        return userDataProviderPort.findByEmail(email).orElseThrow(UserNotFoundException::new);
    }

    private void checkLastSentAt(Date lastSentAt) {
        if (isNull(lastSentAt)) return;

        Calendar limit = Calendar.getInstance();
        limit.add(Calendar.MINUTE, -MINIMUM_LIMIT_RESEND_EMAIL_MINUTES);

        if (lastSentAt.after(limit.getTime())) {
            throw new GenericException("You must wait at least " + MINIMUM_LIMIT_RESEND_EMAIL_MINUTES + " minutes to resend the activation e-mail!");
        }
    }

    private String generateEmailToken() {
        final int MAX_GENERATED_TOKEN_ATTEMPTS = 10;
        final int TOKEN_BYTES = 32;

        for (int i = 0; i < MAX_GENERATED_TOKEN_ATTEMPTS; i++) {
            String randomToken = tokenUtilsPort.generateEmailToken(TOKEN_BYTES);

            if (userDataProviderPort.findByEmailToken(randomToken).isEmpty()) {
                return randomToken;
            }
        }

        throw new GenericException("Error generating email token!");
    }

    private String mountActivationLink(String emailToken) {
        return GATEWAY_URL + "/" + API_SUFFIX + "/user/email_activation/" + emailToken;
    }

    private HashMap<String, Object> fillEmailVariables(User user) {
        return new HashMap<>() {{
            put("${FIRST_NAME}", user.getFirstName());
            put("${ACTIVATION_LINK}", mountActivationLink(user.getEmailToken()));
            put("${EXPIRATION_TIME}", LINK_VALIDATION_IN_DAYS);
        }};
    }

    private User persistUser(User user) {
        return userDataProviderPort.persist(user);
    }

    private void sendEmailActivationEmail(User user) {
        final AsyncEmailOutput email = AsyncEmailOutput.builder()
            .to(user.getEmail())
            .templateName(MAIL_ACTIVATION_TEMPLATE)
            .subject(MAIL_ACTIVATION_SUBJECT)
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
