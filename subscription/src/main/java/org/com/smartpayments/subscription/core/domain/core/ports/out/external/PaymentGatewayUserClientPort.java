package org.com.smartpayments.subscription.core.domain.core.ports.out.external;

import org.com.smartpayments.subscription.core.domain.model.User;

public interface PaymentGatewayUserClientPort {
    String newUserClient(User user);

    void deleteUserClient(User user);
}
