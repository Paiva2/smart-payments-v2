package smartpayments.messaginggateway.infra.adapter.dataprovider;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import smartpayments.messaginggateway.core.domain.model.EmailTemplate;
import smartpayments.messaginggateway.core.ports.out.dataprovider.EmailTemplateDataProviderPort;
import smartpayments.messaginggateway.infra.persistence.repository.EmailTemplateRepository;

import java.util.Optional;

@Component
@AllArgsConstructor
public class EmailTemplateDataProviderAdapter implements EmailTemplateDataProviderPort {
    private final EmailTemplateRepository repository;

    @Override
    public Optional<EmailTemplate> findByName(String name) {
        return repository.findByNameAndActiveIsTrue(name);
    }
}
