package org.com.smartpayments.authenticator.infra.adapters.utils;

import lombok.AllArgsConstructor;
import org.com.smartpayments.authenticator.core.ports.out.utils.PasswordUtilsPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class PasswordUtilsAdapter implements PasswordUtilsPort {
    private final PasswordEncoder passwordEncoder;

    @Override
    public String hashPassword(String password) {
        return passwordEncoder.encode(password);
    }

    @Override
    public boolean comparePassword(String rawPassword, String hash) {
        return passwordEncoder.matches(rawPassword, hash);
    }
}
