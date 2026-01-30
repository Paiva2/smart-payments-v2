package smartpayments.messaginggateway.infra.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import smartpayments.messaginggateway.core.domain.model.EmailTemplate;

import java.util.Optional;

public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Long> {
    Optional<EmailTemplate> findByNameAndActiveIsTrue(String name);
}
