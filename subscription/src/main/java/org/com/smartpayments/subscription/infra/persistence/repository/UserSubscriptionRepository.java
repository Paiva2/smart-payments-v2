package org.com.smartpayments.subscription.infra.persistence.repository;

import org.com.smartpayments.subscription.core.domain.model.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {
    @Query("select usu from UserSubscription usu join fetch usu.plan pla where usu.user.id = :userId")
    Optional<UserSubscription> findByUserIdWithPlan(Long userId);

    @Query(value = """
        select usb.* from users_subscriptions usb
            join users usr on usr.id = usb.user_id
            join plans pln on pln.id = usb.plan_id
        where usb.next_payment_date is not null
            and DATE_TRUNC('day', usb.next_payment_date) = current_date
            and usb.recurrence = 'MONTHLY'
            and usb.external_subscription_id is not null
            and usb.status = 'ACTIVE'
            and pln.type <> 'FREE'
        """, nativeQuery = true)
    List<UserSubscription> findAllMonthlyToRenewNextPaymentDate();

    @Query("select usu from UserSubscription usu join fetch usu.user usr where usu.externalSubscriptionId = :externalSubscriptionId")
    Optional<UserSubscription> findByExternalSubscriptionId(String externalSubscriptionId);
}
