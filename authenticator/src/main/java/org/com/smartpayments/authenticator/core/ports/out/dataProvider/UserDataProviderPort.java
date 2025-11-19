package org.com.smartpayments.authenticator.core.ports.out.dataProvider;

import org.com.smartpayments.authenticator.core.domain.model.User;

import java.util.Optional;

public interface UserDataProviderPort {
    User persist(User user);

    Optional<User> findByEmail(String email);

    Optional<User> findByCpfCnpj(String cpfCnpj);

    Optional<User> findByEmailToken(String emailToken);
}
