package org.com.smartpayments.subscription.infra.persistence.repository;

import org.com.smartpayments.subscription.core.domain.model.PurchaseItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PurchaseItemRepository extends JpaRepository<PurchaseItem, Long> {
    @Query("select puri from PurchaseItem puri " +
        "left join fetch puri.plan " +
        "left join fetch puri.credit " +
        "where puri.purchase.id = :purchaseId")
    List<PurchaseItem> findByPurchaseId(@Param("purchaseId") Long purchaseId);
}
