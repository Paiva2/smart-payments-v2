package org.com.smartpayments.subscription.infra.adapters.dataProvider;

import lombok.AllArgsConstructor;
import org.com.smartpayments.subscription.core.domain.model.CreditConsumptionIdempotency;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.CreditConsumptionIdempotencyDataProviderPort;
import org.com.smartpayments.subscription.infra.persistence.repository.CreditConsumptionIdempotencyRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@AllArgsConstructor
public class CreditConsumptionIdempotencyDataProvideAdapter implements CreditConsumptionIdempotencyDataProviderPort {
    private final CreditConsumptionIdempotencyRepository repository;

    @Override
    public Optional<CreditConsumptionIdempotency> findByKey(String key) {
        return repository.findByIdempotencyKey(key);
    }

    @Override
    public void persist(CreditConsumptionIdempotency creditConsumptionIdempotency) {
        repository.save(creditConsumptionIdempotency);
    }
}
