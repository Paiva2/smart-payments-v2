package org.com.smartpayments.subscription.core.ports.out.external.authenticator;

import org.com.smartpayments.subscription.core.ports.out.external.dto.UserAuthenticatorOutput;

public interface AuthenticatorClientPort {
    UserAuthenticatorOutput findUser(Long userId);
}
