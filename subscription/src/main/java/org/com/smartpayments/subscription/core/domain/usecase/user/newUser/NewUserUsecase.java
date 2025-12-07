package org.com.smartpayments.subscription.core.domain.usecase.user.newUser;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.smartpayments.subscription.core.domain.enums.ECountry;
import org.com.smartpayments.subscription.core.domain.model.Address;
import org.com.smartpayments.subscription.core.domain.model.User;
import org.com.smartpayments.subscription.core.ports.in.UsecaseVoidPort;
import org.com.smartpayments.subscription.core.ports.in.dto.AsyncNewUserInput;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.UserDataProviderPort;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class NewUserUsecase implements UsecaseVoidPort<AsyncNewUserInput> {
    private final UserDataProviderPort userDataProviderPort;

    public void execute(AsyncNewUserInput input) {
        Optional<User> userExists = userDataProviderPort.findById(input.getId());

        if (userExists.isPresent()) {
            log.warn("[NewUserUsecase#execute] - New user message but user already exists, message discarded: {}", input);
            return;
        }

        User user = fillUser(input);
        user = userDataProviderPort.persist(user);
        log.info("[NewUserUsecase#execute] - New user created: {}", user);
    }

    private User fillUser(AsyncNewUserInput input) {
        User user = User.builder()
            .id(input.getId())
            .userPaymentGatewayExternalId(null)
            .firstName(input.getFirstName())
            .lastName(input.getLastName())
            .email(input.getEmail())
            .cpfCnpj(input.getCpfCnpj())
            .type(input.getType())
            .ddi(input.getDdi())
            .phone(input.getPhone())
            .birthdate(input.getBirthdate())
            .active(input.getActive())
            .build();

        Address address = fillAddress(input, user);
        user.setAddress(address);

        return user;
    }

    private Address fillAddress(AsyncNewUserInput input, User user) {
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
}
