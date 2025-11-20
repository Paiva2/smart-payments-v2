package org.com.smartpayments.authenticator.infra.persistence.repository;

import org.com.smartpayments.authenticator.core.domain.model.EmailTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Long> {
    Optional<EmailTemplate> findByNameAndActiveIsTrue(String name);
}
