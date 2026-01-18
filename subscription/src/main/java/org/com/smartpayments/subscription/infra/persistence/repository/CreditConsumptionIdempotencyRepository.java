package org.com.smartpayments.subscription.infra.persistence.repository;

import org.com.smartpayments.subscription.core.domain.model.CreditConsumptionIdempotency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CreditConsumptionIdempotencyRepository extends JpaRepository<CreditConsumptionIdempotency, Long> {
    Optional<CreditConsumptionIdempotency> findByIdempotencyKey(String key);
}
