package org.com.smartpayments.authenticator.infra.adapters.dataProvider;

import lombok.AllArgsConstructor;
import org.com.smartpayments.authenticator.core.domain.enums.ERole;
import org.com.smartpayments.authenticator.core.domain.model.Role;
import org.com.smartpayments.authenticator.core.ports.out.dataProvider.RoleDataProviderPort;
import org.com.smartpayments.authenticator.infra.persistence.repository.RoleRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@AllArgsConstructor
public class RoleDataProviderAdapter implements RoleDataProviderPort {
    private final RoleRepository repository;

    @Override
    public Optional<Role> findByName(ERole name) {
        return repository.findByName(name);
    }
}
