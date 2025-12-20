package org.com.smartpayments.subscription.infra.adapters.dataProvider;

import lombok.AllArgsConstructor;
import org.com.smartpayments.subscription.core.domain.model.PurchaseCharge;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.PurchaseChargeDataProviderPort;
import org.com.smartpayments.subscription.infra.persistence.repository.PurchaseChargeRepository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
@AllArgsConstructor
public class PurchaseChargeDataProviderAdapter implements PurchaseChargeDataProviderPort {
    private final PurchaseChargeRepository repository;

    @Override
    public Optional<PurchaseCharge> findByExternalId(String externalId) {
        return repository.findByExternalId(externalId);
    }

    @Override
    public PurchaseCharge persist(PurchaseCharge purchaseCharge) {
        return repository.save(purchaseCharge);
    }

    @Override
    public void confirmPurchaseChargePayment(String externalChargeId, Date paymentDate) {
        repository.confirmChargePaymentByExternalId(externalChargeId, paymentDate);
    }

    @Override
    public void overduePurchaseChargePayment(String externalChargeId) {
        repository.overdueChargePaymentByExternalId(externalChargeId);
    }

    @Override
    public boolean isConfirmedByExternalId(String externalId) {
        return repository.existsConfirmedByExternalId(externalId);
    }
}
