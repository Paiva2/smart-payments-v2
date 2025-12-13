package org.com.smartpayments.subscription.core.ports.out.dataprovider;

import org.com.smartpayments.subscription.core.domain.enums.EPlan;
import org.com.smartpayments.subscription.core.domain.model.Plan;

import java.util.Optional;

public interface PlanDataProviderPort {
    Optional<Plan> findById(Long id);

    Optional<Plan> findByType(EPlan plan);
}
