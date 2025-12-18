package org.com.smartpayments.subscription.infra.persistence.repository;

import jakarta.persistence.LockModeType;
import org.com.smartpayments.subscription.core.domain.model.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    @Query("select pur from Purchase pur " +
        "join fetch pur.user usr " +
        "join fetch usr.subscription sub " +
        "join fetch pur.items itm " +
        "left join fetch itm.plan pln " +
        "left join fetch itm.credit cdt " +
        "where pur.externalId = :externalId")
    Optional<Purchase> findByExternalId(@Param("externalId") String externalId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select pur from Purchase pur " +
        "join fetch pur.user usr " +
        "join fetch usr.subscription sub " +
        "join fetch pur.items itm " +
        "left join fetch itm.plan pln " +
        "left join fetch itm.credit cdt " +
        "where pur.externalId = :externalId")
    Optional<Purchase> findByExternalIdForUpdate(@Param("externalId") String externalId);

}
