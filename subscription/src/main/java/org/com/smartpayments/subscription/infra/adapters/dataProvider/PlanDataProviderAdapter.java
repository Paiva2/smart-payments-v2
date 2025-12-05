package org.com.smartpayments.subscription.infra.adapters.dataProvider;

import lombok.AllArgsConstructor;
import org.com.smartpayments.subscription.core.domain.model.Plan;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.PlanDataProviderPort;
import org.com.smartpayments.subscription.infra.persistence.repository.PlanRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@AllArgsConstructor
public class PlanDataProviderAdapter implements PlanDataProviderPort {
    private final PlanRepository repository;

    @Override
    public Optional<Plan> findById(Long id) {
        return repository.findById(id);
    }
}
