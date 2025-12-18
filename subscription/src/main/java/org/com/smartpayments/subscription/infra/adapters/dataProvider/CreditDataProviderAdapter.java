package org.com.smartpayments.subscription.infra.adapters.dataProvider;

import lombok.AllArgsConstructor;
import org.com.smartpayments.subscription.core.domain.enums.ECredit;
import org.com.smartpayments.subscription.core.domain.model.Credit;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.CreditDataProviderPort;
import org.com.smartpayments.subscription.infra.persistence.repository.CreditRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@AllArgsConstructor
public class CreditDataProviderAdapter implements CreditDataProviderPort {
    private final CreditRepository repository;

    @Override
    public Optional<Credit> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Optional<Credit> findActiveByType(ECredit credit) {
        return repository.findByTypeAndActiveIsTrue(credit);
    }
}
