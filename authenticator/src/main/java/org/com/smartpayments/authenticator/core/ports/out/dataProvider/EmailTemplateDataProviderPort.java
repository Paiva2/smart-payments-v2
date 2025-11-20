package org.com.smartpayments.authenticator.core.ports.out.dataProvider;

import org.com.smartpayments.authenticator.core.domain.model.EmailTemplate;

import java.util.Optional;

public interface EmailTemplateDataProviderPort {
    Optional<EmailTemplate> findByName(String name);
}
