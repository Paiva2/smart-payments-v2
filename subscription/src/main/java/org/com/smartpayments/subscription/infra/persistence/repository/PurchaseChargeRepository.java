package org.com.smartpayments.subscription.infra.persistence.repository;

import org.com.smartpayments.subscription.core.domain.model.PurchaseCharge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PurchaseChargeRepository extends JpaRepository<PurchaseCharge, Long> {
    Optional<PurchaseCharge> findByExternalId(String externalId);

    List<PurchaseCharge> findByPurchaseId(Long purchaseId);
}
