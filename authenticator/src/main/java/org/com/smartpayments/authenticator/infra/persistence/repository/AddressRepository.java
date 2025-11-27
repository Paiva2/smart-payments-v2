package org.com.smartpayments.authenticator.infra.persistence.repository;

import org.com.smartpayments.authenticator.core.domain.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {
    Optional<Address> findByUserId(Long userId);
}
