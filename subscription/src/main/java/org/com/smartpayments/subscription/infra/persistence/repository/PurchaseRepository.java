package org.com.smartpayments.subscription.infra.persistence.repository;

import org.com.smartpayments.subscription.core.domain.model.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    Optional<Purchase> findByExternalId(String externalId);
    
}
