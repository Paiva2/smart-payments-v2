package org.com.smartpayments.subscription.infra.persistence.repository;

import org.com.smartpayments.subscription.core.domain.model.UserSubscriptionCreditHistory;
import org.com.smartpayments.subscription.core.ports.out.projections.GetUserCreditsResumeProjectionOutput;
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

    @Query(value = """
        SELECT
            COALESCE(SUM(amount) FILTER (
                WHERE credit_type = 'EMAIL'
                  AND expires_at IS NULL
                  AND (valid_from <= now() OR valid_from IS NULL)
            ), 0) AS emailCredits,
        
            COALESCE(SUM(amount) FILTER (
                WHERE credit_type = 'EMAIL'
                  AND expires_at IS NOT NULL
                  AND expires_at >= now()
                  AND (valid_from <= now() OR valid_from IS NULL)
            ), 0) AS emailSubscriptionCredits,
        
            COALESCE(SUM(amount) FILTER (
                WHERE credit_type = 'SMS'
                  AND expires_at IS NOT NULL
                  AND expires_at >= now()
                  AND (valid_from <= now() OR valid_from IS NULL)
            ), 0) AS smsSubscriptionCredits,
        
            COALESCE(SUM(amount) FILTER (
                WHERE credit_type = 'SMS'
                  AND expires_at IS NULL
                  AND (valid_from <= now() OR valid_from IS NULL)
            ), 0) AS smsCredits,
        
            COALESCE(SUM(amount) FILTER (
                WHERE credit_type = 'WHATS_APP'
                  AND expires_at IS NULL
                  AND (valid_from <= now() OR valid_from IS NULL)
            ), 0) AS whatsAppCredits,
        
            COALESCE(SUM(amount) FILTER (
                WHERE credit_type = 'WHATS_APP'
                  AND expires_at IS NOT NULL
                  AND expires_at >= now()
                  AND (valid_from <= now() OR valid_from IS NULL)
            ), 0) AS whatsAppSubscriptionCredits
        
        FROM (
            SELECT amount, credit_type, expires_at, valid_from
            FROM users_subscriptions_credits_history
            FOR UPDATE
        ) usbch;
        """, nativeQuery = true)
    GetUserCreditsResumeProjectionOutput getUserCreditsResumeWithLocking(@Param("userSubscriptionId") Long userSubscriptionId);
}
