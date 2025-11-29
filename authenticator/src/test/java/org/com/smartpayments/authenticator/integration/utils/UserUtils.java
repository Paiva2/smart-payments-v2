package org.com.smartpayments.authenticator.integration.utils;

import com.github.javafaker.Faker;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.com.smartpayments.authenticator.core.common.exception.RoleNotFoundException;
import org.com.smartpayments.authenticator.core.domain.enums.EBrState;
import org.com.smartpayments.authenticator.core.domain.enums.ECountry;
import org.com.smartpayments.authenticator.core.domain.enums.ERole;
import org.com.smartpayments.authenticator.core.domain.enums.EUserType;
import org.com.smartpayments.authenticator.core.domain.model.Address;
import org.com.smartpayments.authenticator.core.domain.model.Role;
import org.com.smartpayments.authenticator.core.domain.model.User;
import org.com.smartpayments.authenticator.core.domain.model.UserRole;
import org.com.smartpayments.authenticator.core.ports.out.dataProvider.RoleDataProviderPort;
import org.com.smartpayments.authenticator.core.ports.out.dataProvider.UserDataProviderPort;
import org.com.smartpayments.authenticator.core.ports.out.utils.JwtUtilsPort;
import org.com.smartpayments.authenticator.core.ports.out.utils.PasswordUtilsPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static org.com.smartpayments.authenticator.integration.constants.TestConstants.*;

@Component
@AllArgsConstructor
public class UserUtils {
    private final Faker faker = new Faker();

    private final UserDataProviderPort userDataProviderPort;
    private final RoleDataProviderPort roleDataProviderPort;
    private final PasswordUtilsPort passwordUtilsPort;
    private final JwtUtilsPort jwtUtilsPort;

    @SneakyThrows
    @Transactional
    public User createUser(ERole role, Boolean active, Boolean emailConfirmed) {
        User newUser = User.builder()
            .firstName(faker.name().fullName())
            .lastName(faker.name().lastName())
            .email(faker.internet().emailAddress().toLowerCase(Locale.ROOT))
            .passwordHash(passwordUtilsPort.hashPassword(RANDOM_VALID_PASSWORD))
            .cpfCnpj(RANDOM_VALID_CPF)
            .type(EUserType.NATURAL)
            .phone(RANDOM_VALID_PHONE_NUMBER)
            .ddi("+55")
            .birthdate(new SimpleDateFormat("dd-MM-yyyy").parse(RANDOM_VALID_BIRTHDATE))
            .active(active)
            .emailConfirmedAt(emailConfirmed ? new Date() : null)
            .emailToken("any_token")
            .emailTokenSentAt(new Date())
            .userRoles(new ArrayList<>())
            .build();

        final Address address = fillAddress(newUser);
        final UserRole userRole = fillUserRole(role, newUser);

        newUser.setAddress(address);
        newUser.getUserRoles().add(userRole);

        return userDataProviderPort.persist(newUser);
    }

    public String generateAuthToken(Long userId) {
        return jwtUtilsPort.generateAuthJwt(userId, 1);
    }

    private UserRole fillUserRole(ERole role, User user) {
        Role roleFound = roleDataProviderPort.findByName(role)
            .orElseThrow(() -> new RoleNotFoundException(role.toString()));

        return UserRole.builder()
            .id(new UserRole.UserRoleId(null, roleFound.getId()))
            .user(user)
            .role(roleFound)
            .build();
    }

    private Address fillAddress(User user) {
        return Address.builder()
            .street(faker.address().streetName())
            .neighborhood("test_neighborhood")
            .number(faker.address().streetAddressNumber())
            .zipcode(faker.numerify("#####-###"))
            .city(faker.address().city())
            .state(EBrState.SP)
            .complement("test_complement")
            .country(ECountry.BR)
            .user(user)
            .build();
    }
}
