package org.com.smartpayments.subscription.core.ports.out.dataprovider;

import org.com.smartpayments.subscription.core.domain.model.User;

import java.util.Optional;

public interface UserDataProviderPort {
    Optional<User> findActiveById(Long id);

    User persist(User user);
}
