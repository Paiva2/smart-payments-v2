package org.com.smartpayments.subscription.core.domain.core.usecase.purchaseCharge.createPurchaseCharge;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.smartpayments.subscription.core.domain.common.exception.PurchaseNotFoundException;
import org.com.smartpayments.subscription.core.domain.core.ports.in.UsecaseVoidPort;
import org.com.smartpayments.subscription.core.domain.core.ports.in.dto.CreatePurchaseChargeInput;
import org.com.smartpayments.subscription.core.domain.core.ports.out.dataprovider.PurchaseChargeDataProviderPort;
import org.com.smartpayments.subscription.core.domain.core.ports.out.dataprovider.PurchaseDataProviderPort;
import org.com.smartpayments.subscription.core.domain.model.Purchase;
import org.com.smartpayments.subscription.core.domain.model.PurchaseCharge;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
@Service
@AllArgsConstructor
public class CreatePurchaseChargeUsecase implements UsecaseVoidPort<CreatePurchaseChargeInput> {
    private final PurchaseChargeDataProviderPort purchaseChargeDataProviderPort;
    private final PurchaseDataProviderPort purchaseDataProviderPort;

    @Override
    public void execute(CreatePurchaseChargeInput input) {
        Optional<PurchaseCharge> purchaseCharge = findPurchaseCharge(input.getExternalChargeId());

        if (purchaseCharge.isPresent()) {
            log.info("[CreatePurchaseChargeUsecase#execute] - Charge already exists, ignoring creation. id: {}", input.getExternalChargeId());
            return;
        }

        log.info("[CreatePurchaseChargeUsecase#execute] - Charge being created. id: {}", input.getExternalChargeId());

        PurchaseCharge newPurchaseCharge = fillPurchaseCharge(input);
        persistPurchaseCharge(newPurchaseCharge);
    }

    private Optional<PurchaseCharge> findPurchaseCharge(String externalId) {
        return purchaseChargeDataProviderPort.findByExternalId(externalId);
    }

    private PurchaseCharge fillPurchaseCharge(CreatePurchaseChargeInput input) {
        return PurchaseCharge.builder()
            .externalId(input.getExternalChargeId())
            .totalValue(input.getValue())
            .status(input.getStatus())
            .paymentDate(input.getPaymentDate())
            .dueDate(input.getDueDate())
            .paymentUrl(isNull(input.getInvoiceUrl()) ? input.getBankSlipUrl() : input.getInvoiceUrl())
            .purchase(isNull(input.getPurchase()) && isNull(input.getPurchaseId()) ? null : handlePurchase(input))
            .paymentDate(input.getPaymentDate())
            .build();
    }

    private void persistPurchaseCharge(PurchaseCharge purchaseCharge) {
        purchaseChargeDataProviderPort.persist(purchaseCharge);
    }

    private Purchase handlePurchase(CreatePurchaseChargeInput input) {
        if (nonNull(input.getPurchase())) return input.getPurchase();

        return purchaseDataProviderPort.findByExternalId(input.getExternalChargeId())
            .orElseThrow(() -> new PurchaseNotFoundException("Purchase provided not found!"));
    }
}
