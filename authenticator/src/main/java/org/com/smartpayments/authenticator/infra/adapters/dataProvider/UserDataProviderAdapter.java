package org.com.smartpayments.authenticator.infra.adapters.dataProvider;

import lombok.AllArgsConstructor;
import org.com.smartpayments.authenticator.core.domain.model.User;
import org.com.smartpayments.authenticator.core.ports.out.dataProvider.UserDataProviderPort;
import org.com.smartpayments.authenticator.infra.persistence.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Optional;

@Component
@AllArgsConstructor
public class UserDataProviderAdapter implements UserDataProviderPort {
    private final UserRepository repository;

    @Override
    public User persist(User user) {
        return repository.save(user);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email.toLowerCase(Locale.ROOT).trim());
    }

    @Override
    public Optional<User> findByCpfCnpj(String cpfCnpj) {
        return repository.findByCpfCnpj(cpfCnpj.replaceAll("\\D", ""));
    }

    @Override
    public Optional<User> findByEmailToken(String emailToken) {
        return repository.findByEmailToken(emailToken);
    }
}
