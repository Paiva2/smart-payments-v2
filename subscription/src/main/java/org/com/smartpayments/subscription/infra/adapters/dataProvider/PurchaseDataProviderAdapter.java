package org.com.smartpayments.subscription.infra.adapters.dataProvider;

import lombok.AllArgsConstructor;
import org.com.smartpayments.subscription.core.domain.model.Purchase;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.PurchaseDataProviderPort;
import org.com.smartpayments.subscription.infra.persistence.repository.PurchaseRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@AllArgsConstructor
public class PurchaseDataProviderAdapter implements PurchaseDataProviderPort {
    private final PurchaseRepository repository;

    @Override
    public Purchase persist(Purchase purchase) {
        return repository.save(purchase);
    }

    @Override
    public Optional<Purchase> findByExternalId(String externalId) {
        return repository.findByExternalId(externalId);
    }

    @Override
    public Optional<Purchase> findByExternalIdLocking(String externalId) {
        return repository.findByExternalIdForUpdate(externalId);
    }
}
