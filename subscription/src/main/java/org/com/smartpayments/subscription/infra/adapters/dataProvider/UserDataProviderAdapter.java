package org.com.smartpayments.subscription.infra.adapters.dataProvider;

import lombok.AllArgsConstructor;
import org.com.smartpayments.subscription.core.domain.model.User;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.UserDataProviderPort;
import org.com.smartpayments.subscription.infra.persistence.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@AllArgsConstructor
public class UserDataProviderAdapter implements UserDataProviderPort {
    private final UserRepository repository;

    @Override
    public Optional<User> findActiveById(Long id) {
        return repository.findByIdAndActiveIsTrue(id);
    }

    @Override
    public Optional<User> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Optional<User> findByPaymentGatewayId(String paymentGatewayId) {
        return repository.findByUserPaymentGatewayExternalId(paymentGatewayId);
    }

    @Override
    public User persist(User user) {
        return repository.save(user);
    }
}
