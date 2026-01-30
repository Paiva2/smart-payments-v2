package smartpayments.messaginggateway.core.ports.out.dataprovider;

import smartpayments.messaginggateway.core.domain.model.EmailTemplate;

import java.util.Optional;

public interface EmailTemplateDataProviderPort {
    Optional<EmailTemplate> findByName(String name);
}
