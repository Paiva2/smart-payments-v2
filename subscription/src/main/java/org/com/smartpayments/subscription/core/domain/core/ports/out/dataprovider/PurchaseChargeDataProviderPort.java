package org.com.smartpayments.subscription.core.domain.core.ports.out.dataprovider;

import org.com.smartpayments.subscription.core.domain.model.PurchaseCharge;

import java.util.Optional;

public interface PurchaseChargeDataProviderPort {
    Optional<PurchaseCharge> findByExternalId(String externalId);

    PurchaseCharge persist(PurchaseCharge purchaseCharge);
}
