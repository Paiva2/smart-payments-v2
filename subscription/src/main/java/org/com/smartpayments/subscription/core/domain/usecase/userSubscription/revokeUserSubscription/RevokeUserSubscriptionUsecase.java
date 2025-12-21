package org.com.smartpayments.subscription.core.domain.usecase.userSubscription.revokeUserSubscription;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.smartpayments.subscription.core.common.exception.PlanNotFoundException;
import org.com.smartpayments.subscription.core.common.exception.UserSubscriptionNotFoundException;
import org.com.smartpayments.subscription.core.domain.enums.EPlan;
import org.com.smartpayments.subscription.core.domain.enums.ESubscriptionStatus;
import org.com.smartpayments.subscription.core.domain.model.Plan;
import org.com.smartpayments.subscription.core.domain.model.User;
import org.com.smartpayments.subscription.core.domain.model.UserSubscription;
import org.com.smartpayments.subscription.core.ports.in.UsecaseVoidPort;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.PlanDataProviderPort;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.UserSubscriptionCreditHistoryDataProviderPort;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.UserSubscriptionCreditRecurrenceDataProviderPort;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.UserSubscriptionDataProviderPort;
import org.com.smartpayments.subscription.core.ports.out.dto.AsyncSubscriptionPlanStateInput;
import org.com.smartpayments.subscription.core.ports.out.external.paymentGateway.PaymentGatewaySubscriptionClientPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@AllArgsConstructor
public class RevokeUserSubscriptionUsecase implements UsecaseVoidPort<AsyncSubscriptionPlanStateInput> {
    private final UserSubscriptionDataProviderPort userSubscriptionDataProviderPort;
    private final UserSubscriptionCreditRecurrenceDataProviderPort userSubscriptionCreditRecurrenceDataProviderPort;
    private final UserSubscriptionCreditHistoryDataProviderPort userSubscriptionCreditHistoryDataProviderPort;
    private final PlanDataProviderPort planDataProviderPort;
    private final PaymentGatewaySubscriptionClientPort paymentGatewaySubscriptionClientPort;

    @Override
    @Transactional
    public void execute(AsyncSubscriptionPlanStateInput input) {
        UserSubscription userSubscription = findUserSubscription(input.getUserSubscriptionId());

        log.info("[RevokeUserSubscriptionUsecase#execute] - Starting subscription revoke. User Subscription Id: {}", userSubscription.getId());

        String subscriptionExternalId = userSubscription.getExternalSubscriptionId();

        setUserSubscriptionPlanFree(userSubscription);
        deleteCreditRecurrences(userSubscription);
        invalidateCurrentCredits(userSubscription);
        deleteSubscriptionPaymentGateway(userSubscription.getUser(), subscriptionExternalId);

        log.info("[RevokeUserSubscriptionUsecase#execute] - Subscription revoked. User Subscription Id: {}", userSubscription.getId());
    }

    private UserSubscription findUserSubscription(Long userSubscriptionId) {
        return userSubscriptionDataProviderPort.findById(userSubscriptionId)
            .orElseThrow(UserSubscriptionNotFoundException::new);
    }

    private void setUserSubscriptionPlanFree(UserSubscription userSubscription) {
        Plan freePlan = findFreePlan();

        userSubscription.setExpiredAt(null);
        userSubscription.setStatus(ESubscriptionStatus.ACTIVE);
        userSubscription.setExternalSubscriptionId(null);
        userSubscription.setPlan(freePlan);
        userSubscription.setValue(BigDecimal.ZERO);
        userSubscription.setRecurrence(null);
        userSubscription.setNextPaymentDate(null);
        userSubscription.setUnlimitedWhatsAppCredits(freePlan.getUnlimitedWhatsAppCredits());
        userSubscription.setUnlimitedEmailCredits(freePlan.getUnlimitedEmailCredits());
        userSubscription.setUnlimitedSmsCredits(freePlan.getUnlimitedSmsCredits());

        userSubscriptionDataProviderPort.persist(userSubscription);
    }

    private void deleteCreditRecurrences(UserSubscription userSubscription) {
        userSubscriptionCreditRecurrenceDataProviderPort.deleteAllByUserSubscription(userSubscription.getId());
    }

    private void invalidateCurrentCredits(UserSubscription userSubscription) {
        userSubscriptionCreditHistoryDataProviderPort.revokeNonExpiredCredits(userSubscription.getId());
    }

    private Plan findFreePlan() {
        return planDataProviderPort.findByType(EPlan.FREE)
            .orElseThrow(() -> new PlanNotFoundException("Plan not found! Plan: " + EPlan.FREE));
    }

    private void deleteSubscriptionPaymentGateway(User user, String subscriptionExternalId) {
        paymentGatewaySubscriptionClientPort.deleteSubscription(user, subscriptionExternalId);
    }
}
