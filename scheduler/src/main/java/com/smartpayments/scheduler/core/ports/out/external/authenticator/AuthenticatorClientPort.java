package com.smartpayments.scheduler.core.ports.out.external.authenticator;

import com.smartpayments.scheduler.core.ports.out.external.dto.UserAuthenticatorOutput;

public interface AuthenticatorClientPort {
    UserAuthenticatorOutput findUser(Long userId);
}