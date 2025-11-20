package org.com.smartpayments.authenticator.infra.adapters.dataProvider;

import lombok.AllArgsConstructor;
import org.com.smartpayments.authenticator.core.domain.model.EmailTemplate;
import org.com.smartpayments.authenticator.core.ports.out.dataProvider.EmailTemplateDataProviderPort;
import org.com.smartpayments.authenticator.infra.persistence.repository.EmailTemplateRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@AllArgsConstructor
public class EmailTemplateDataProviderImpl implements EmailTemplateDataProviderPort {
    private final EmailTemplateRepository repository;

    @Override
    public Optional<EmailTemplate> findByName(String name) {
        return repository.findByNameAndActiveIsTrue(name);
    }
}
