package org.com.smartpayments.subscription.infra.persistence.repository;

import org.com.smartpayments.subscription.core.domain.model.UserSubscriptionCreditHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserSubscriptionCreditHistoryRepository extends JpaRepository<UserSubscriptionCreditHistory, Long> {
    List<UserSubscriptionCreditHistory> findAllByUserSubscriptionId(Long userSubscriptionId);

    @Modifying
    @Query("update UserSubscriptionCreditHistory " +
        "set expiresAt = current_timestamp " +
        "where transactionType = 'SAMPLE_GRANT' " +
        "and userSubscription.id = :userSubscriptionId " +
        "and expiresAt is null")
    void revokeSampleCreditsByUserSubscriptionId(@Param("userSubscriptionId") Long userSubscriptionId);

    @Modifying
    @Query("update UserSubscriptionCreditHistory " +
        "set expiresAt = current_timestamp " +
        "where transactionType = 'GRANT' " +
        "and userSubscription.id = :userSubscriptionId " +
        "and (expiresAt is not null and expiresAt >= current_date)")
    void revokeNonExpiredCreditsByUserSubscriptionId(@Param("userSubscriptionId") Long userSubscriptionId);
}
