package org.com.smartpayments.authenticator.infra.adapters.dataProvider;

import lombok.AllArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.com.smartpayments.authenticator.core.ports.out.dataProvider.AuthenticationBlackListDataProviderPort;
import org.com.smartpayments.authenticator.infra.persistence.repository.AuthenticationBlackListRepository;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class AuthenticationBlackListDataProviderAdapter implements AuthenticationBlackListDataProviderPort {
    private AuthenticationBlackListRepository repository;

    @Override
    public boolean existsByTokenHash(String token) {
        return repository.existsByTokenHash(DigestUtils.sha256Hex(token));
    }
}
