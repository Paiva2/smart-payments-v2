package org.com.smartpayments.subscription.core.domain.usecase.userSubscription.renewUserSubscription;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.smartpayments.subscription.core.common.exception.UserSubscriptionNotFoundException;
import org.com.smartpayments.subscription.core.domain.enums.ECreditTransactionType;
import org.com.smartpayments.subscription.core.domain.enums.ESubscriptionRecurrence;
import org.com.smartpayments.subscription.core.domain.enums.ESubscriptionStatus;
import org.com.smartpayments.subscription.core.domain.model.Credit;
import org.com.smartpayments.subscription.core.domain.model.UserSubscription;
import org.com.smartpayments.subscription.core.domain.model.UserSubscriptionCreditHistory;
import org.com.smartpayments.subscription.core.domain.model.UserSubscriptionCreditRecurrence;
import org.com.smartpayments.subscription.core.ports.in.UsecaseVoidPort;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.UserSubscriptionCreditHistoryDataProviderPort;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.UserSubscriptionCreditRecurrenceDataProviderPort;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.UserSubscriptionDataProviderPort;
import org.com.smartpayments.subscription.core.ports.out.dto.AsyncRenewSubscriptionPlanInput;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class RenewUserSubscriptionUsecase implements UsecaseVoidPort<AsyncRenewSubscriptionPlanInput> {
    private final UserSubscriptionDataProviderPort userSubscriptionDataProviderPort;
    private final UserSubscriptionCreditRecurrenceDataProviderPort userSubscriptionCreditRecurrenceDataProviderPort;
    private final UserSubscriptionCreditHistoryDataProviderPort userSubscriptionCreditHistoryDataProviderPort;

    @Override
    @Transactional
    public void execute(AsyncRenewSubscriptionPlanInput input) {
        UserSubscription userSubscription = findUserSubscription(input);

        userSubscription.setNextPaymentDate(setNextPaymentDate(userSubscription));
        userSubscription.setStatus(ESubscriptionStatus.ACTIVE);
        persistUserSubscription(userSubscription);

        log.info("[RenewUserSubscriptionUsecase#execute] - Subscription renewed. Id: {}", userSubscription.getId());

        List<UserSubscriptionCreditRecurrence> creditsRecurrence = findCreditsRecurrence(userSubscription);

        if (!creditsRecurrence.isEmpty()) {
            handleSubscriptionCreditRecurrence(userSubscription, creditsRecurrence);
            log.info("[RenewUserSubscriptionUsecase#execute] - Subscription credits renewed. Id: {}", userSubscription.getId());
        }
    }

    private UserSubscription findUserSubscription(AsyncRenewSubscriptionPlanInput input) {
        return userSubscriptionDataProviderPort.findByUserWithPlan(input.getUserId())
            .orElseThrow(UserSubscriptionNotFoundException::new);
    }

    private Date setNextPaymentDate(UserSubscription userSubscription) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(userSubscription.getNextPaymentDate());
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 59);

        if (userSubscription.getRecurrence().equals(ESubscriptionRecurrence.MONTHLY)) {
            calendar.add(Calendar.MONTH, 1);
        }

        return calendar.getTime();
    }

    private void persistUserSubscription(UserSubscription userSubscription) {
        userSubscriptionDataProviderPort.persist(userSubscription);
    }

    private List<UserSubscriptionCreditRecurrence> findCreditsRecurrence(UserSubscription userSubscription) {
        return userSubscriptionCreditRecurrenceDataProviderPort.findAllByUserSubscription(userSubscription.getId());
    }

    private void handleSubscriptionCreditRecurrence(UserSubscription userSubscription, List<UserSubscriptionCreditRecurrence> creditsRecurrence) {
        List<UserSubscriptionCreditHistory> credits = new ArrayList<>();

        for (UserSubscriptionCreditRecurrence creditRecurrence : creditsRecurrence) {
            Credit credit = creditRecurrence.getCredit();

            credits.add(UserSubscriptionCreditHistory.builder()
                .amount(creditRecurrence.getQuantity())
                .creditType(credit.getType())
                .transactionType(ECreditTransactionType.GRANT)
                .validFrom(new Date())
                .expiresAt(defineCreditExpiration(userSubscription.getNextPaymentDate()))
                .userSubscription(userSubscription)
                .build()
            );
        }

        userSubscriptionCreditHistoryDataProviderPort.persistAll(credits);
    }

    private Date defineCreditExpiration(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
}
