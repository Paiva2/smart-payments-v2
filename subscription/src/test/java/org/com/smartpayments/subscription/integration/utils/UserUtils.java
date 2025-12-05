package org.com.smartpayments.subscription.integration.utils;

import com.github.javafaker.Faker;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.com.smartpayments.subscription.core.domain.enums.EBrState;
import org.com.smartpayments.subscription.core.domain.enums.ECountry;
import org.com.smartpayments.subscription.core.domain.enums.EUserType;
import org.com.smartpayments.subscription.core.domain.model.Address;
import org.com.smartpayments.subscription.core.domain.model.User;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.UserDataProviderPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Locale;

import static org.com.smartpayments.subscription.integration.constants.TestConstants.*;


@Component
@AllArgsConstructor
public class UserUtils {
    private final Faker faker = new Faker();

    private final UserDataProviderPort userDataProviderPort;
    private final JwtUtilsTest jwtUtilsTest;

    @SneakyThrows
    @Transactional
    public User createUser(Boolean active, Boolean userRegisteredPaymentGateway) {
        User newUser = User.builder()
            .id(1L)
            .userPaymentGatewayExternalId(userRegisteredPaymentGateway ? "payment_gateway_external_id" : null)
            .firstName(faker.name().fullName())
            .lastName(faker.name().lastName())
            .email(faker.internet().emailAddress().toLowerCase(Locale.ROOT))
            .cpfCnpj(RANDOM_VALID_CPF)
            .type(EUserType.NATURAL)
            .ddi("+55")
            .phone(RANDOM_VALID_PHONE_NUMBER)
            .birthdate(new SimpleDateFormat("dd-MM-yyyy").parse(RANDOM_VALID_BIRTHDATE))
            .active(active)
            .build();

        final Address address = fillAddress(newUser);

        newUser.setAddress(address);

        return userDataProviderPort.persist(newUser);
    }

    @SneakyThrows
    @Transactional
    public User createUser(Long id, String cpf, Boolean active, Boolean userRegisteredPaymentGateway) {
        User newUser = User.builder()
            .id(id)
            .userPaymentGatewayExternalId(userRegisteredPaymentGateway ? "payment_gateway_external_id" : null)
            .firstName(faker.name().fullName())
            .lastName(faker.name().lastName())
            .email(faker.internet().emailAddress().toLowerCase(Locale.ROOT))
            .cpfCnpj(cpf)
            .type(EUserType.NATURAL)
            .ddi("+55")
            .phone(RANDOM_VALID_PHONE_NUMBER)
            .birthdate(new SimpleDateFormat("dd-MM-yyyy").parse(RANDOM_VALID_BIRTHDATE))
            .active(active)
            .build();

        final Address address = fillAddress(newUser);

        newUser.setAddress(address);

        return userDataProviderPort.persist(newUser);
    }

    public String generateAuthToken(Long userId) {
        return jwtUtilsTest.generateAuthJwt(userId, 1);
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
