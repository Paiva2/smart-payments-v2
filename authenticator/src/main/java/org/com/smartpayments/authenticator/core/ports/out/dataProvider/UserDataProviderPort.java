package org.com.smartpayments.authenticator.core.ports.out.dataProvider;

import org.com.smartpayments.authenticator.core.domain.model.User;

import java.util.Optional;

public interface UserDataProviderPort {
    User persist(User user);

    Optional<User> findActiveById(Long id);

    Optional<User> findByIdWithRoles(Long id);

    Optional<User> findActiveByIdWithDependencies(Long id);

    Optional<User> findByEmail(String email);

    Optional<User> findActiveByEmail(String email);

    Optional<User> findByCpfCnpj(String cpfCnpj);

    Optional<User> findByEmailToken(String emailToken);

    Optional<User> findActiveByPasswordToken(String passwordToken);
}
