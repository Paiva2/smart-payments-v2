package org.com.smartpayments.authenticator.core.ports.out.dataProvider;

import org.com.smartpayments.authenticator.core.domain.enums.ERole;
import org.com.smartpayments.authenticator.core.domain.model.Role;

import java.util.Optional;

public interface RoleDataProviderPort {
    Optional<Role> findByName(ERole name);
}
