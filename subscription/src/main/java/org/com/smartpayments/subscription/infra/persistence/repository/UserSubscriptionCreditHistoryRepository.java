package org.com.smartpayments.subscription.infra.persistence.repository;

import org.com.smartpayments.subscription.core.domain.model.UserSubscriptionCreditHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserSubscriptionCreditHistoryRepository extends JpaRepository<UserSubscriptionCreditHistory, Long> {
    List<UserSubscriptionCreditHistory> findAllByUserSubscriptionId(Long userSubscriptionId);
}
