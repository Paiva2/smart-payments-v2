package org.com.smartpayments.subscription.core.domain.usecase.purchaseCharge.refundedPurchaseCharge;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.smartpayments.subscription.core.common.exception.PurchaseChargeNotFoundException;
import org.com.smartpayments.subscription.core.common.exception.UserSubscriptionNotFoundException;
import org.com.smartpayments.subscription.core.domain.enums.ECreditTransactionType;
import org.com.smartpayments.subscription.core.domain.enums.EPlan;
import org.com.smartpayments.subscription.core.domain.enums.EPurchaseChargeStatus;
import org.com.smartpayments.subscription.core.domain.enums.EUserSubscriptionState;
import org.com.smartpayments.subscription.core.domain.model.*;
import org.com.smartpayments.subscription.core.ports.in.UsecaseVoidPort;
import org.com.smartpayments.subscription.core.ports.in.dto.RefundedPurchaseChargeInput;
import org.com.smartpayments.subscription.core.ports.in.utils.MessageUtilsPort;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.PurchaseChargeDataProviderPort;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.PurchaseItemDataProviderPort;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.UserSubscriptionCreditHistoryDataProviderPort;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.UserSubscriptionDataProviderPort;
import org.com.smartpayments.subscription.core.ports.out.dto.AsyncMessageOutput;
import org.com.smartpayments.subscription.core.ports.out.dto.AsyncSubscriptionPlanStateInput;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefundedPurchaseChargeUsecase implements UsecaseVoidPort<RefundedPurchaseChargeInput> {
    private final static ObjectMapper mapper = new ObjectMapper();

    private final PurchaseChargeDataProviderPort purchaseChargeDataProviderPort;
    private final PurchaseItemDataProviderPort purchaseItemDataProviderPort;
    private final UserSubscriptionDataProviderPort userSubscriptionDataProviderPort;
    private final UserSubscriptionCreditHistoryDataProviderPort userSubscriptionCreditHistoryDataProviderPort;

    private final MessageUtilsPort messageUtilsPort;

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${spring.kafka.topics.user-subscription-states}")
    private String userSubscriptionStatesTopic;

    @Override
    @Transactional
    public void execute(RefundedPurchaseChargeInput input) {
        PurchaseCharge purchaseCharge = findPurchaseCharge(input.getExternalChargeId());

        Purchase purchase = purchaseCharge.getPurchase();
        User user = purchase.getUser();
        UserSubscription userSubscription = findUserSubscription(user.getId());
        List<PurchaseItem> items = findPurchaseItems(purchase.getId());

        if (input.isFromSubscription()) {
            rollbackSubscription(purchase.getExternalId(), user.getId(), userSubscription);
        } else {
            rollbackCreditsRefunded(userSubscription, items);
        }

        purchaseCharge.setStatus(EPurchaseChargeStatus.REFUNDED);
        persistPurchaseCharge(purchaseCharge);
    }

    private PurchaseCharge findPurchaseCharge(String externalId) {
        return purchaseChargeDataProviderPort.findByExternalId(externalId)
            .orElseThrow(PurchaseChargeNotFoundException::new);
    }

    private UserSubscription findUserSubscription(Long userId) {
        return userSubscriptionDataProviderPort.findByUserWithPlan(userId)
            .orElseThrow(UserSubscriptionNotFoundException::new);
    }

    private void rollbackSubscription(String externalPurchaseId, Long userId, UserSubscription userSubscription) {
        if (userSubscription.getPlan().getType().equals(EPlan.FREE) ||
            !userSubscription.getExternalSubscriptionId().equals(externalPurchaseId)) return;

        sendMessageToRevokeSubscription(userId, userSubscription);
    }

    private List<PurchaseItem> findPurchaseItems(Long purchaseId) {
        return purchaseItemDataProviderPort.findByPurchaseId(purchaseId);
    }

    private void rollbackCreditsRefunded(UserSubscription userSubscription, List<PurchaseItem> items) {
        List<UserSubscriptionCreditHistory> creditsRefunded = new ArrayList<>();

        for (PurchaseItem item : items) {
            creditsRefunded.add(UserSubscriptionCreditHistory.builder()
                .amount(-item.getQuantity())
                .creditType(item.getCredit().getType())
                .transactionType(ECreditTransactionType.REFUND)
                .sourceUsage(null)
                .sourceUsageId(null)
                .validFrom(null)
                .expiresAt(null)
                .userSubscription(userSubscription)
                .build()
            );
        }

        userSubscriptionCreditHistoryDataProviderPort.persistAll(creditsRefunded);
    }

    private void persistPurchaseCharge(PurchaseCharge purchaseCharge) {
        purchaseChargeDataProviderPort.persist(purchaseCharge);
    }

    private void sendMessageToRevokeSubscription(Long userId, UserSubscription userSubscription) {
        try {
            kafkaTemplate.send(userSubscriptionStatesTopic, mapper.writeValueAsString(input(userId, userSubscription)));
        } catch (Exception e) {
            log.info("[RevokeSubscriptionPlansJob#sendMessageToRevoke - Error while sending message to expire subscription. Error: {}", e.getMessage());
        }
    }

    private AsyncMessageOutput<Object> input(Long userId, UserSubscription userSubscription) {
        final String issuer = "SUBSCRIPTION";

        final AsyncSubscriptionPlanStateInput renewSubscriptionInput = AsyncSubscriptionPlanStateInput.builder()
            .state(EUserSubscriptionState.EXPIRED)
            .userSubscriptionId(userSubscription.getId())
            .userId(userId)
            .build();

        return AsyncMessageOutput.builder()
            .messageHash(messageUtilsPort.generateMessageHash(issuer))
            .issuer(issuer)
            .timestamp(new Date())
            .data(renewSubscriptionInput)
            .build();
    }
}
