package org.com.smartpayments.authenticator.infra.adapters.utils;

import org.com.smartpayments.authenticator.core.ports.out.utils.TokenUtilsPort;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

@Component
public class TokenUtilsAdapter implements TokenUtilsPort {

    @Override
    public String generateEmailToken(int bytes) {
        SecureRandom random = new SecureRandom();
        byte[] buffer = new byte[bytes];
        random.nextBytes(buffer);

        return Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(buffer);
    }
}
