package org.com.smartpayments.authenticator.core.domain.usecase.user.changeEmail;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.smartpayments.authenticator.core.common.exception.GenericException;
import org.com.smartpayments.authenticator.core.common.exception.UserEmailNotActiveException;
import org.com.smartpayments.authenticator.core.common.exception.UserNotFoundException;
import org.com.smartpayments.authenticator.core.domain.model.User;
import org.com.smartpayments.authenticator.core.domain.usecase.user.registerUser.exception.EmailAlreadyUsedException;
import org.com.smartpayments.authenticator.core.domain.usecase.user.sendActiveEmail.SendActiveEmailUsecase;
import org.com.smartpayments.authenticator.core.ports.in.UsecaseVoidPort;
import org.com.smartpayments.authenticator.core.ports.in.dto.ChangeEmailInput;
import org.com.smartpayments.authenticator.core.ports.in.dto.SendActiveEmailInput;
import org.com.smartpayments.authenticator.core.ports.out.dataProvider.UserDataProviderPort;
import org.com.smartpayments.authenticator.core.ports.out.dto.AsyncMessageOutput;
import org.com.smartpayments.authenticator.core.ports.out.dto.AsyncUpdateUserOutput;
import org.com.smartpayments.authenticator.core.ports.out.dto.UserProfileOutput;
import org.com.smartpayments.authenticator.core.ports.out.utils.MessageUtilsPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import static java.util.Objects.isNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChangeEmailUsecase implements UsecaseVoidPort<ChangeEmailInput> {
    private final static ObjectMapper mapper = new ObjectMapper();

    private final UserDataProviderPort userDataProviderPort;
    private final SendActiveEmailUsecase sendActiveEmailUsecase;

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final MessageUtilsPort messageUtilsPort;

    @Value("${spring.kafka.topics.update-user}")
    private String userUpdateTopic;

    @Override
    public void execute(ChangeEmailInput input) {
        User user = findUser(input.getUserId());

        if (isNull(user.getEmailConfirmedAt())) {
            throw new UserEmailNotActiveException();
        }

        if (Objects.equals(user.getEmail(), input.getNewEmail().toLowerCase(Locale.ROOT))) {
            throw new GenericException("New e-mail is already equals current e-mail!");
        }

        checkNewEmailUsed(input.getNewEmail());

        user.setEmailConfirmedAt(null);
        user.setEmail(input.getNewEmail().toLowerCase(Locale.ROOT));

        sendActiveEmailUsecase.execute(new SendActiveEmailInput(user, user.getEmail()));
        sendMessageUpdate(user.toProfileOutput());
    }

    private User findUser(Long userId) {
        return userDataProviderPort.findActiveById(userId).orElseThrow(UserNotFoundException::new);
    }

    private void checkNewEmailUsed(String newEmail) {
        userDataProviderPort.findByEmail(newEmail)
            .ifPresent(user -> {
                throw new EmailAlreadyUsedException();
            });
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
