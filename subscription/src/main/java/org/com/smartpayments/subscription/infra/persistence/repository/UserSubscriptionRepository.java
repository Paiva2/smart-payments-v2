package org.com.smartpayments.subscription.infra.persistence.repository;

import org.com.smartpayments.subscription.core.domain.model.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {
    @Query("select usu from UserSubscription usu join fetch usu.plan pla where usu.user.id = :userId")
    Optional<UserSubscription> findByUserIdWithPlan(Long userId);
}
