package org.com.smartpayments.authenticator.core.domain.usecase.user.registerUser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.smartpayments.authenticator.core.common.exception.GenericCpfCnpjInvalidException;
import org.com.smartpayments.authenticator.core.common.exception.GenericException;
import org.com.smartpayments.authenticator.core.common.exception.GenericPasswordInvalidException;
import org.com.smartpayments.authenticator.core.common.exception.RoleNotFoundException;
import org.com.smartpayments.authenticator.core.domain.enums.ECountry;
import org.com.smartpayments.authenticator.core.domain.enums.ERole;
import org.com.smartpayments.authenticator.core.domain.enums.EUserType;
import org.com.smartpayments.authenticator.core.domain.model.Address;
import org.com.smartpayments.authenticator.core.domain.model.Role;
import org.com.smartpayments.authenticator.core.domain.model.User;
import org.com.smartpayments.authenticator.core.domain.model.UserRole;
import org.com.smartpayments.authenticator.core.domain.usecase.user.registerUser.exception.DocumentAlreadyUsedException;
import org.com.smartpayments.authenticator.core.domain.usecase.user.registerUser.exception.EmailAlreadyUsedException;
import org.com.smartpayments.authenticator.core.ports.in.UsecaseVoidPort;
import org.com.smartpayments.authenticator.core.ports.in.dto.RegisterUserInput;
import org.com.smartpayments.authenticator.core.ports.out.dataProvider.AsyncMessageDataProviderPort;
import org.com.smartpayments.authenticator.core.ports.out.dataProvider.RoleDataProviderPort;
import org.com.smartpayments.authenticator.core.ports.out.dataProvider.UserDataProviderPort;
import org.com.smartpayments.authenticator.core.ports.out.dto.AsyncEmailOutput;
import org.com.smartpayments.authenticator.core.ports.out.dto.AsyncMessageOutput;
import org.com.smartpayments.authenticator.core.ports.out.dto.AsyncNewUserOutput;
import org.com.smartpayments.authenticator.core.ports.out.utils.MessageUtilsPort;
import org.com.smartpayments.authenticator.core.ports.out.utils.PasswordUtilsPort;
import org.com.smartpayments.authenticator.core.ports.out.utils.PersonalDocumentUtilsPort;
import org.com.smartpayments.authenticator.core.ports.out.utils.TokenUtilsPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.isNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegisterUserUsecase implements UsecaseVoidPort<RegisterUserInput> {
    private final static ObjectMapper mapper = new ObjectMapper();
    private final static Integer LINK_EXPIRATION_IN_DAYS = 1;
    private final static String MAIL_ACTIVATION_TEMPLATE = "user-email-activation";
    private final static String MAIL_ACTIVATION_SUBJECT = "Welcome to smart payments!";

    private final UserDataProviderPort userDataProviderPort;
    private final RoleDataProviderPort roleDataProviderPort;
    private final AsyncMessageDataProviderPort asyncMessageDataProviderPort;

    private final PersonalDocumentUtilsPort personalDocumentUtilsPort;
    private final PasswordUtilsPort passwordUtilsPort;
    private final TokenUtilsPort tokenUtilsPort;
    private final MessageUtilsPort messageUtilsPort;

    @Value("${spring.kafka.topics.mail-sender}")
    private String SEND_EMAIL_TOPIC;

    @Value("${spring.kafka.topics.new-user}")
    private String NEW_USER_TOPIC;

    @Value("${kong.url}")
    private String GATEWAY_URL;

    @Value("${server.api-suffix}")
    private String API_SUFFIX;

    @Override
    @Transactional
    public void execute(RegisterUserInput input) {
        validatePassword(input.getPassword());
        validateDocument(input);
        checkEmailUsed(input.getEmail());
        checkDocumentUsed(input.getCpfCnpj());

        User user = fillUser(input);
        persistUser(user);

        sendEmailActivationEmail(user);
        sendAsyncNewUserMessage(user);
    }

    private void checkEmailUsed(String email) {
        Optional<User> user = userDataProviderPort.findByEmail(email);

        if (user.isPresent()) {
            throw new EmailAlreadyUsedException();
        }
    }

    private void validatePassword(String password) {
        final int MAX_BYTES_PASSWORD = 72;

        if (password.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES_PASSWORD) {
            throw new GenericPasswordInvalidException("Password is too long!");
        }
    }

    private void validateDocument(RegisterUserInput input) {
        if (isNull(input.getType())) {
            input.setType(EUserType.NATURAL);
        }

        if (Objects.equals(input.getType(), EUserType.NATURAL) && !personalDocumentUtilsPort.isValidCpf(input.getCpfCnpj())) {
            throw new GenericCpfCnpjInvalidException("Invalid Cpf!");
        }

        if (Objects.equals(input.getType(), EUserType.LEGAL) && !personalDocumentUtilsPort.isValidCnpj(input.getCpfCnpj())) {
            throw new GenericCpfCnpjInvalidException("Invalid Cnpj!");
        }
    }

    private void checkDocumentUsed(String document) {
        Optional<User> user = userDataProviderPort.findByCpfCnpj(document);

        if (user.isPresent()) {
            throw new DocumentAlreadyUsedException();
        }
    }

    private Address fillAddress(RegisterUserInput input, User user) {
        return Address.builder()
            .street(input.getAddress().getStreet())
            .neighborhood(input.getAddress().getNeighborhood())
            .number(input.getAddress().getNumber())
            .zipcode(input.getAddress().getZipcode())
            .complement(input.getAddress().getComplement())
            .city(input.getAddress().getCity())
            .state(input.getAddress().getState())
            .country(ECountry.BR)
            .user(user)
            .build();
    }

    private UserRole fillUserRole(User user) {
        Role role = roleDataProviderPort.findByName(ERole.MEMBER)
            .orElseThrow(() -> new RoleNotFoundException(ERole.MEMBER.toString()));

        return UserRole.builder()
            .id(new UserRole.UserRoleId(null, role.getId()))
            .user(user)
            .role(role)
            .build();
    }

    private String generateEmailToken() {
        final int MAX_GENERATED_TOKEN_ATTEMPTS = 10;
        final int TOKEN_BYTES = 32;

        for (int i = 0; i < MAX_GENERATED_TOKEN_ATTEMPTS; i++) {
            String randomToken = tokenUtilsPort.generateUrlBasedToken(TOKEN_BYTES);

            if (userDataProviderPort.findByEmailToken(randomToken).isEmpty()) {
                return randomToken;
            }
        }

        throw new GenericException("Error generating email token!");
    }

    private Date convertDate(String dateStr) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
            return formatter.parse(dateStr);
        } catch (ParseException exception) {
            log.error(exception.getMessage());
            throw new GenericException("Error while parsing date!");
        }
    }

    private User fillUser(RegisterUserInput input) {
        User newUser = User.builder()
            .firstName(input.getFirstName())
            .lastName(input.getLastName())
            .email(input.getEmail().toLowerCase(Locale.ROOT).trim())
            .passwordHash(passwordUtilsPort.hashPassword(input.getPassword()))
            .cpfCnpj(input.getCpfCnpj())
            .type(input.getType())
            .ddi("+55")
            .phone(input.getPhone())
            .birthdate(convertDate(input.getBirthdate()))
            .active(true)
            .emailToken(generateEmailToken())
            .emailTokenSentAt(new Date())
            .userRoles(new ArrayList<>())
            .build();

        final Address address = fillAddress(input, newUser);
        final UserRole userRole = fillUserRole(newUser);

        newUser.setAddress(address);
        newUser.getUserRoles().add(userRole);

        return newUser;
    }

    private void persistUser(User user) {
        userDataProviderPort.persist(user);
    }

    private String mountActivationLink(String emailToken) {
        return GATEWAY_URL + "/" + API_SUFFIX + "/user/email_activation/" + emailToken;
    }

    private HashMap<String, Object> fillEmailVariables(User user) {
        return new HashMap<>() {{
            put("${FIRST_NAME}", user.getFirstName());
            put("${ACTIVATION_LINK}", mountActivationLink(user.getEmailToken()));
            put("${EXPIRATION_TIME}", LINK_EXPIRATION_IN_DAYS + " day");
        }};
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

    private void sendAsyncNewUserMessage(User user) {
        final AsyncNewUserOutput userOutput = AsyncNewUserOutput.builder()
            .id(user.getId())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .email(user.getEmail())
            .cpfCnpj(user.getCpfCnpj())
            .type(user.getType())
            .ddi(user.getDdi())
            .phone(user.getPhone())
            .birthdate(user.getBirthdate())
            .active(true)
            .emailConfirmedAt(user.getEmailConfirmedAt())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .address(AsyncNewUserOutput.AsyncAddressOutput.builder()
                .street(user.getAddress().getStreet())
                .neighborhood(user.getAddress().getNeighborhood())
                .number(user.getAddress().getNumber())
                .zipcode(user.getAddress().getZipcode())
                .complement(user.getAddress().getComplement())
                .city(user.getAddress().getCity())
                .state(user.getAddress().getState())
                .country(user.getAddress().getCountry())
                .build()
            ).build();

        final String messageIssuer = "AUTHENTICATOR";

        AsyncMessageOutput<AsyncNewUserOutput> output =
            new AsyncMessageOutput<>(messageUtilsPort.generateMessageHash(messageIssuer), new Date(), messageIssuer, userOutput);

        try {
            asyncMessageDataProviderPort.sendMessage(NEW_USER_TOPIC, user.getId().toString(), mapper.writeValueAsString(output));
        } catch (JsonProcessingException e) {
            String message = "Error while sending new user message!";
            log.error(message, e);
            throw new GenericException(message);
        }
    }
}
