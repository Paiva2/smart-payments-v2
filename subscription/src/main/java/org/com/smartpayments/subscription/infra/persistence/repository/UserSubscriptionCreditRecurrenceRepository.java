package org.com.smartpayments.subscription.infra.persistence.repository;

import org.com.smartpayments.subscription.core.domain.model.UserSubscriptionCreditRecurrence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserSubscriptionCreditRecurrenceRepository extends JpaRepository<UserSubscriptionCreditRecurrence, Long> {
    @Query("select usbcr from UserSubscriptionCreditRecurrence usbcr join fetch usbcr.credit cdt where usbcr.userSubscription.id = :userSubscriptionId")
    List<UserSubscriptionCreditRecurrence> findAllByUserSubscriptionId(Long userSubscriptionId);

    @Modifying
    void deleteAllByUserSubscriptionId(Long userSubscriptionId);
}
