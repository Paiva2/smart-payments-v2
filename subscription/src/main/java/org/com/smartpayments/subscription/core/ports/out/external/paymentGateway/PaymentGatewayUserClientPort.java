package org.com.smartpayments.subscription.core.ports.out.external.paymentGateway;

import org.com.smartpayments.subscription.core.domain.model.User;

public interface PaymentGatewayUserClientPort {
    String newUserClient(User user);

    void deleteUserClient(User user);
}
