package org.com.smartpayments.subscription.infra.persistence.repository;

import org.com.smartpayments.subscription.core.domain.model.PurchaseItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseItemRepository extends JpaRepository<PurchaseItem, Long> {
    List<PurchaseItem> findByPurchaseId(Long purchaseId);
}
