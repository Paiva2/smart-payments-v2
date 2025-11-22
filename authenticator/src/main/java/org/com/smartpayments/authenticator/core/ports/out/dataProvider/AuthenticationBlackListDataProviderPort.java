package org.com.smartpayments.authenticator.core.ports.out.dataProvider;

public interface AuthenticationBlackListDataProviderPort {
    boolean existsByTokenHash(String token);
}
