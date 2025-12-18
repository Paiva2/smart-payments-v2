package org.com.smartpayments.subscription.infra.persistence.repository;

import org.com.smartpayments.subscription.core.domain.model.views.UserSubscriptionResumeView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserSubscriptionViewResumeRepository extends JpaRepository<UserSubscriptionResumeView, Long> {
    Optional<UserSubscriptionResumeView> findByUserId(Long userId);
}
