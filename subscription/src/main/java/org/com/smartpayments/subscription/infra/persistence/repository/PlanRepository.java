package org.com.smartpayments.subscription.infra.persistence.repository;

import org.com.smartpayments.subscription.core.domain.enums.EPlan;
import org.com.smartpayments.subscription.core.domain.model.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlanRepository extends JpaRepository<Plan, Long> {
    Optional<Plan> findByType(EPlan type);
}
