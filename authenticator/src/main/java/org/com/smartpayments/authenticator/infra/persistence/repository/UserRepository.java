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
        "where usr.id = :id and usr.active = true")
    Optional<User> findByIdActiveWithRoles(@Param("id") Long id);

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndActiveIsTrue(String email);

    Optional<User> findByCpfCnpj(String cpfCnpj);

    Optional<User> findByEmailToken(String token);
}
