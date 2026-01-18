package org.com.smartpayments.subscription.core.ports.out.dataprovider;

import org.com.smartpayments.subscription.core.domain.model.User;

import java.util.Optional;

public interface UserDataProviderPort {
    Optional<User> findActiveById(Long id);

    Optional<User> findActiveWithSubscriptionById(Long id);

    Optional<User> findById(Long id);

    Optional<User> findByPaymentGatewayId(String paymentGatewayId);

    User persist(User user);
}
