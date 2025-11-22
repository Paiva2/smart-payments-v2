package org.com.smartpayments.authenticator.infra.persistence.repository;

import org.com.smartpayments.authenticator.core.domain.model.AuthenticationBlackList;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthenticationBlackListRepository extends JpaRepository<AuthenticationBlackList, Long> {
    boolean existsByTokenHash(String tokenHash);
}
