package org.com.smartpayments.subscription.core.domain.core.ports.out.dataprovider;

import org.com.smartpayments.subscription.core.domain.model.Credit;

import java.util.Optional;

public interface CreditDataProviderPort {
    Optional<Credit> findById(Long id);
}
