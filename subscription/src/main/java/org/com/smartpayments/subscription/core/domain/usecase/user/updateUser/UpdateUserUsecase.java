package org.com.smartpayments.subscription.core.domain.usecase.user.updateUser;

import lombok.AllArgsConstructor;
import org.com.smartpayments.subscription.core.common.exception.UserNotFoundException;
import org.com.smartpayments.subscription.core.domain.model.Address;
import org.com.smartpayments.subscription.core.domain.model.User;
import org.com.smartpayments.subscription.core.ports.in.UsecaseVoidPort;
import org.com.smartpayments.subscription.core.ports.in.dto.AsyncUpdateUserInput;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.UserDataProviderPort;
import org.springframework.stereotype.Service;

import static java.util.Objects.nonNull;

@Service
@AllArgsConstructor
public class UpdateUserUsecase implements UsecaseVoidPort<AsyncUpdateUserInput> {
    private final UserDataProviderPort userDataProviderPort;

    @Override
    public void execute(AsyncUpdateUserInput input) {
        User user = findUser(input.getId());

        updateUser(user, input);
        persistUser(user);
    }

    private User findUser(Long userId) {
        return userDataProviderPort.findById(userId)
            .orElseThrow(UserNotFoundException::new);
    }

    private void updateUser(User user, AsyncUpdateUserInput input) {
        user.setFirstName(input.getFirstName());
        user.setLastName(input.getLastName());
        user.setEmail(input.getEmail());
        user.setCpfCnpj(input.getCpfCnpj());
        user.setType(input.getType());
        user.setDdi(input.getDdi());
        user.setPhone(input.getPhone());
        user.setBirthdate(input.getBirthdate());
        user.setActive(input.getActive() && nonNull(input.getEmailConfirmedAt()));

        if (input.getAddress() != null) {
            if (user.getAddress() == null) {
                user.setAddress(new Address());
            }

            user.getAddress().setStreet(input.getAddress().getStreet());
            user.getAddress().setNeighborhood(input.getAddress().getNeighborhood());
            user.getAddress().setNumber(input.getAddress().getNumber());
            user.getAddress().setZipcode(input.getAddress().getZipcode());
            user.getAddress().setComplement(input.getAddress().getComplement());
            user.getAddress().setCity(input.getAddress().getCity());
            user.getAddress().setState(input.getAddress().getState());
            user.getAddress().setCountry(input.getAddress().getCountry());
        }
    }

    private void persistUser(User user) {
        userDataProviderPort.persist(user);
    }
}
