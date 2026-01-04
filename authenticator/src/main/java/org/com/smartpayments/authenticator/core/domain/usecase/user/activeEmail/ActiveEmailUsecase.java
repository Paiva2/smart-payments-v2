package org.com.smartpayments.authenticator.core.domain.usecase.user.activeEmail;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.smartpayments.authenticator.core.common.exception.GenericException;
import org.com.smartpayments.authenticator.core.common.exception.UserNotActiveException;
import org.com.smartpayments.authenticator.core.common.exception.UserNotFoundException;
import org.com.smartpayments.authenticator.core.domain.model.User;
import org.com.smartpayments.authenticator.core.domain.usecase.user.activeEmail.exception.EmailTokenExpiredException;
import org.com.smartpayments.authenticator.core.domain.usecase.user.activeEmail.exception.UserEmailAlreadyActiveException;
import org.com.smartpayments.authenticator.core.ports.in.UsecaseVoidPort;
import org.com.smartpayments.authenticator.core.ports.out.dataProvider.UserDataProviderPort;
import org.com.smartpayments.authenticator.core.ports.out.dto.AsyncMessageOutput;
import org.com.smartpayments.authenticator.core.ports.out.dto.AsyncUpdateUserOutput;
import org.com.smartpayments.authenticator.core.ports.out.dto.UserProfileOutput;
import org.com.smartpayments.authenticator.core.ports.out.utils.MessageUtilsPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;

import static java.util.Objects.nonNull;
import static org.com.smartpayments.authenticator.core.common.constants.Constants.MAX_EXPIRATION_DAYS_EMAIL_ACTIVATION_TOKEN;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActiveEmailUsecase implements UsecaseVoidPort<String> {
    private final static ObjectMapper mapper = new ObjectMapper();

    private final UserDataProviderPort userDataProviderPort;

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final MessageUtilsPort messageUtilsPort;

    @Value("${spring.kafka.topics.update-user}")
    private String userUpdateTopic;

    @Override
    public void execute(String input) {
        User user = findUserByEmailToken(input);

        if (!user.getActive()) {
            throw new UserNotActiveException();
        }

        if (nonNull(user.getEmailConfirmedAt())) {
            throw new UserEmailAlreadyActiveException();
        }

        validateTokenSentAtLimit(user.getEmailTokenSentAt());

        user.setEmailConfirmedAt(new Date());
        persistUser(user);

        sendMessageUpdate(user.toProfileOutput());
    }

    private User findUserByEmailToken(String email) {
        return userDataProviderPort.findByEmailToken(email).orElseThrow(UserNotFoundException::new);
    }

    private void validateTokenSentAtLimit(Date tokenSentAt) {
        Calendar limit = Calendar.getInstance();
        limit.add(Calendar.DAY_OF_MONTH, -MAX_EXPIRATION_DAYS_EMAIL_ACTIVATION_TOKEN);

        if (tokenSentAt.before(limit.getTime())) {
            throw new EmailTokenExpiredException();
        }
    }

    private void persistUser(User user) {
        userDataProviderPort.persist(user);
    }

    private AsyncUpdateUserOutput mountAsyncUpdateUserOutput(UserProfileOutput userProfileOutput) {
        return AsyncUpdateUserOutput.builder()
            .id(userProfileOutput.getId())
            .firstName(userProfileOutput.getFirstName())
            .lastName(userProfileOutput.getLastName())
            .email(userProfileOutput.getEmail())
            .cpfCnpj(userProfileOutput.getCpfCnpj())
            .type(userProfileOutput.getType())
            .ddi(userProfileOutput.getDdi())
            .phone(userProfileOutput.getPhone())
            .birthdate(userProfileOutput.getBirthdate())
            .active(userProfileOutput.getActive())
            .emailConfirmedAt(userProfileOutput.getEmailConfirmedAt())
            .address(AsyncUpdateUserOutput.AsyncAddressOutput.builder()
                .street(userProfileOutput.getAddress().getStreet())
                .neighborhood(userProfileOutput.getAddress().getNeighborhood())
                .number(userProfileOutput.getAddress().getNumber())
                .zipcode(userProfileOutput.getAddress().getZipcode())
                .complement(userProfileOutput.getAddress().getComplement())
                .city(userProfileOutput.getAddress().getCity())
                .state(userProfileOutput.getAddress().getState())
                .country(userProfileOutput.getAddress().getCountry()
                ).build()
            ).build();
    }

    private void sendMessageUpdate(UserProfileOutput userProfileOutput) {
        String issuer = "AUTHENTICATOR";

        try {
            AsyncUpdateUserOutput updateUser = mountAsyncUpdateUserOutput(userProfileOutput);

            AsyncMessageOutput<AsyncUpdateUserOutput> asyncMessage = new AsyncMessageOutput<>(
                messageUtilsPort.generateMessageHash(issuer),
                new Date(),
                issuer,
                updateUser
            );

            kafkaTemplate.send(userUpdateTopic, userProfileOutput.getId().toString(), mapper.writeValueAsString(asyncMessage));
        } catch (JsonProcessingException exception) {
            String message = "Error while sending user update message: {}";
            log.error(message, exception.getMessage(), exception);
            throw new GenericException("Error while updating user!");
        }
    }
}
