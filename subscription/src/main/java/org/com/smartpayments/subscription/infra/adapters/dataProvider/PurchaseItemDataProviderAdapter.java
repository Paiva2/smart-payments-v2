package org.com.smartpayments.subscription.infra.adapters.dataProvider;

import lombok.AllArgsConstructor;
import org.com.smartpayments.subscription.core.domain.model.PurchaseItem;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.PurchaseItemDataProviderPort;
import org.com.smartpayments.subscription.infra.persistence.repository.PurchaseItemRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class PurchaseItemDataProviderAdapter implements PurchaseItemDataProviderPort {
    private final PurchaseItemRepository purchaseItemRepository;

    @Override
    public List<PurchaseItem> findByPurchaseId(Long purchaseId) {
        return purchaseItemRepository.findByPurchaseId(purchaseId);
    }
}
