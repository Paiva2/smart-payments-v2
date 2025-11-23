package org.com.smartpayments.authenticator.infra.persistence.repository;

import org.com.smartpayments.authenticator.core.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @Query("select usr from User usr " +
        "join fetch usr.userRoles url " +
        "join fetch url.role rol " +
        "where usr.id = :id")
    Optional<User> findByIdWithRoles(@Param("id") Long id);

    @Query("select usr from User usr " +
        "join fetch usr.address adr " +
        "join fetch usr.userRoles url " +
        "join fetch url.role rol " +
        "where usr.id = :id and usr.active = true")
    Optional<User> findByIdActiveWithDeps(@Param("id") Long id);

    Optional<User> findByEmail(String email);

    Optional<User> findByIdAndActiveIsTrueAndEmailConfirmedAtNotNull(@Param("id") Long id);

    Optional<User> findByEmailAndActiveIsTrue(String email);

    Optional<User> findByCpfCnpj(String cpfCnpj);

    Optional<User> findByEmailToken(String token);

    Optional<User> findByPasswordTokenAndActiveIsTrue(String token);
}
