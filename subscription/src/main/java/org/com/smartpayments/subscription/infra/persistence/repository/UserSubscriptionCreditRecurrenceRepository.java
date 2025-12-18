package org.com.smartpayments.subscription.infra.persistence.repository;

import org.com.smartpayments.subscription.core.domain.model.UserSubscriptionCreditRecurrence;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSubscriptionCreditRecurrenceRepository extends JpaRepository<UserSubscriptionCreditRecurrence, Long> {
}
