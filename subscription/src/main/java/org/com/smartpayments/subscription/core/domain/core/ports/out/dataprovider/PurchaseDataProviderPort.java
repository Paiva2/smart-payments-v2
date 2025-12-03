package org.com.smartpayments.subscription.core.domain.core.ports.out.dataprovider;

import org.com.smartpayments.subscription.core.domain.model.Purchase;

import java.util.Optional;

public interface PurchaseDataProviderPort {
    Purchase persist(Purchase purchase);

    Optional<Purchase> findByExternalId(String externalId);
}
