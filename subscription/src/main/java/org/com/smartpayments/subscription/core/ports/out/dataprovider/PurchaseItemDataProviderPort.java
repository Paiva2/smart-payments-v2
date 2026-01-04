package org.com.smartpayments.subscription.core.ports.out.dataprovider;

import org.com.smartpayments.subscription.core.domain.model.PurchaseItem;

import java.util.List;

public interface PurchaseItemDataProviderPort {
    List<PurchaseItem> findByPurchaseId(Long purchaseId);
}
