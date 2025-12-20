package org.com.smartpayments.subscription.infra.persistence.repository;

import org.com.smartpayments.subscription.core.domain.model.PurchaseCharge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface PurchaseChargeRepository extends JpaRepository<PurchaseCharge, Long> {
    Optional<PurchaseCharge> findByExternalId(String externalId);

    List<PurchaseCharge> findByPurchaseId(Long purchaseId);

    @Modifying
    @Query("update PurchaseCharge " +
        "set status = 'CONFIRMED', " +
        "paymentDate = :paymentDate " +
        "where externalId = :externalId"
    )
    void confirmChargePaymentByExternalId(@Param("externalId") String externalId, @Param("paymentDate") Date paymentDate);

    @Modifying
    @Query("update PurchaseCharge " +
        "set status = 'OVERDUE' " +
        "where externalId = :externalId"
    )
    void overdueChargePaymentByExternalId(@Param("externalId") String externalId);

    @Query("select exists(select puc from PurchaseCharge puc where puc.externalId = :externalId and puc.status = 'CONFIRMED')")
    boolean existsConfirmedByExternalId(@Param("externalId") String externalId);
}
