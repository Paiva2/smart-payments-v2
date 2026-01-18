package org.com.smartpayments.subscription.infra.persistence.repository;

import org.com.smartpayments.subscription.core.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByIdAndActiveIsTrue(Long id);

    Optional<User> findByUserPaymentGatewayExternalId(String id);

    @Query("select usr from User usr " +
        "join fetch usr.subscription sub " +
        "join fetch sub.plan pln " +
        "where usr.id = :id")
    Optional<User> findByIdWithUserSubscription(@Param("id") Long id);
}
