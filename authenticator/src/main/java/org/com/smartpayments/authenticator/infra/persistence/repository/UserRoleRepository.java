package org.com.smartpayments.authenticator.infra.persistence.repository;

import org.com.smartpayments.authenticator.core.domain.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRole.UserRoleId> {
    @Query("select url from UserRole url join fetch url.role rl where url.user.id = :userId")
    List<UserRole> findByUserId(@Param("userId") Long userId);
}
