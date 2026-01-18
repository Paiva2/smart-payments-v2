package org.com.smartpayments.subscription.core.ports.out.dataprovider;

import org.com.smartpayments.subscription.core.domain.model.CreditConsumptionIdempotency;

import java.util.Optional;

public interface CreditConsumptionIdempotencyDataProviderPort {
    Optional<CreditConsumptionIdempotency> findByKey(String key);

    void persist(CreditConsumptionIdempotency creditConsumptionIdempotency);
}
