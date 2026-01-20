package org.com.smartpayments.subscription.core.domain.usecase.userSubscriptionCreditsHistory.consumeUserCreditsInternal;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.smartpayments.subscription.core.common.exception.UserNotFoundException;
import org.com.smartpayments.subscription.core.domain.enums.ECredit;
import org.com.smartpayments.subscription.core.domain.enums.ECreditTransactionType;
import org.com.smartpayments.subscription.core.domain.model.CreditConsumptionIdempotency;
import org.com.smartpayments.subscription.core.domain.model.User;
import org.com.smartpayments.subscription.core.domain.model.UserSubscription;
import org.com.smartpayments.subscription.core.domain.model.UserSubscriptionCreditHistory;
import org.com.smartpayments.subscription.core.domain.usecase.userSubscription.cancelSubscription.event.SubscriptionCancelledEvent;
import org.com.smartpayments.subscription.core.domain.usecase.userSubscription.sendUserSubscriptionUpdateMessage.SendUserSubscriptionUpdateMessageUsecase;
import org.com.smartpayments.subscription.core.ports.in.UsecasePort;
import org.com.smartpayments.subscription.core.ports.in.dto.ConsumeUserCreditsInternalInput;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.CreditConsumptionIdempotencyDataProviderPort;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.UserDataProviderPort;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.UserSubscriptionCreditHistoryDataProviderPort;
import org.com.smartpayments.subscription.core.ports.out.dto.ConsumeUserCreditsInternalOutput;
import org.com.smartpayments.subscription.core.ports.out.projections.GetUserCreditsResumeProjectionOutput;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.*;

import static java.util.Objects.isNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsumeUserCreditsInternalUsecase implements UsecasePort<ConsumeUserCreditsInternalInput, ConsumeUserCreditsInternalOutput> {
    private final static ObjectMapper mapper = new ObjectMapper();
    private final static String EXTERNAL_CREDIT_CONSUMPTION_SUCCESS = "SUCCESS";
    private final static String EXTERNAL_CREDIT_CONSUMPTION_NO_BALANCE = "NO_BALANCE_AVAILABLE";

    private final UserDataProviderPort userDataProviderPort;
    private final UserSubscriptionCreditHistoryDataProviderPort userSubscriptionCreditHistoryDataProviderPort;
    private final CreditConsumptionIdempotencyDataProviderPort creditConsumptionIdempotencyDataProviderPort;

    private final SendUserSubscriptionUpdateMessageUsecase sendUserSubscriptionUpdateMessageUsecase;

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional
    public ConsumeUserCreditsInternalOutput execute(ConsumeUserCreditsInternalInput input) {
        Optional<CreditConsumptionIdempotency> findIdempotency = findResultByIdempotencyKey(input.getIdempotencyKey());

        if (findIdempotency.isPresent()) {
            return findIdempotency.get().getData();
        }

        User user = findUser(input.getUserId());
        UserSubscription userSubscription = user.getSubscription();
        GetUserCreditsResumeProjectionOutput userCreditsResume = getUserCreditsResume(userSubscription.getId());
        ConsumeUserCreditsInternalOutput output = new ConsumeUserCreditsInternalOutput();
        output.setUserId(user.getId());

        handleCreditsConsumption(userSubscription, input, userCreditsResume, output);

        persistIdempotency(input.getIdempotencyKey(), output);

        applicationEventPublisher.publishEvent(new SubscriptionCancelledEvent(userSubscription.getUser()));

        return output;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSubscriptionCancelled(SubscriptionCancelledEvent event) {
        sendUserSubscriptionUpdateMessage(event.getUser());
    }

    private Optional<CreditConsumptionIdempotency> findResultByIdempotencyKey(String key) {
        return creditConsumptionIdempotencyDataProviderPort.findByKey(key);
    }

    private User findUser(Long userId) {
        return userDataProviderPort.findActiveWithSubscriptionById(userId)
            .orElseThrow(UserNotFoundException::new);
    }

    private GetUserCreditsResumeProjectionOutput getUserCreditsResume(Long userSubscriptionId) {
        return userSubscriptionCreditHistoryDataProviderPort.userSubscriptionCreditsResumeWithLocking(userSubscriptionId);
    }

    private void handleCreditsConsumption(UserSubscription userSubscription, ConsumeUserCreditsInternalInput input, GetUserCreditsResumeProjectionOutput userCreditsResume, ConsumeUserCreditsInternalOutput output) {
        List<UserSubscriptionCreditHistory> creditsHistory = new ArrayList<>();

        if (input.getEmailCredits() > 0) {
            if (userCreditsResume.getEmailCredits() < input.getEmailCredits() && userCreditsResume.getEmailSubscriptionCredits() < input.getEmailCredits()) {
                output.setEmail(EXTERNAL_CREDIT_CONSUMPTION_NO_BALANCE);
            } else {
                output.setEmail(EXTERNAL_CREDIT_CONSUMPTION_SUCCESS);

                if (!userSubscription.getUnlimitedEmailCredits()) {
                    Date creditConsumptionExpiration = userCreditsResume.getEmailCredits() > 0 ? null : userSubscription.getNextPaymentDate();

                    creditsHistory.add(mountCreditHistory(userSubscription, input.getEmailCredits(), creditConsumptionExpiration, ECredit.EMAIL, input.getUsageReason(), input.getUsageReasonId()));
                }
            }
        }

        if (input.getSmsCredits() > 0) {
            if (userCreditsResume.getSmsCredits() < input.getSmsCredits() && userCreditsResume.getSmsSubscriptionCredits() < input.getSmsCredits()) {
                output.setSms(EXTERNAL_CREDIT_CONSUMPTION_NO_BALANCE);
            } else {
                output.setSms(EXTERNAL_CREDIT_CONSUMPTION_SUCCESS);

                if (!userSubscription.getUnlimitedSmsCredits()) {
                    Date creditConsumptionExpiration = userCreditsResume.getSmsCredits() > 0 ? null : userSubscription.getNextPaymentDate();

                    creditsHistory.add(mountCreditHistory(userSubscription, input.getSmsCredits(), creditConsumptionExpiration, ECredit.SMS, input.getUsageReason(), input.getUsageReasonId()));
                }
            }
        }

        if (input.getWhatsAppCredits() > 0) {
            if (userCreditsResume.getWhatsAppCredits() < input.getWhatsAppCredits() && userCreditsResume.getWhatsAppSubscriptionCredits() < input.getWhatsAppCredits()) {
                output.setWhatsapp(EXTERNAL_CREDIT_CONSUMPTION_NO_BALANCE);
            } else {
                output.setWhatsapp(EXTERNAL_CREDIT_CONSUMPTION_SUCCESS);

                if (!userSubscription.getUnlimitedWhatsAppCredits()) {
                    Date creditConsumptionExpiration = userCreditsResume.getWhatsAppCredits() > 0 ? null : userSubscription.getNextPaymentDate();

                    creditsHistory.add(mountCreditHistory(userSubscription, input.getWhatsAppCredits(), creditConsumptionExpiration, ECredit.WHATS_APP, input.getUsageReason(), input.getUsageReasonId()));
                }
            }
        }

        persistConsumedCredits(creditsHistory);
    }

    private void persistConsumedCredits(List<UserSubscriptionCreditHistory> creditsHistory) {
        userSubscriptionCreditHistoryDataProviderPort.persistAll(creditsHistory);
    }

    private void persistIdempotency(String key, ConsumeUserCreditsInternalOutput result) {
        creditConsumptionIdempotencyDataProviderPort.persist(CreditConsumptionIdempotency.builder()
            .idempotencyKey(key)
            .data(result)
            .build()
        );
    }

    private UserSubscriptionCreditHistory mountCreditHistory(UserSubscription userSubscription, int amount, Date expiresAt, ECredit creditType, String sourceUsage, String sourceId) {
        return UserSubscriptionCreditHistory.builder()
            .amount(-amount)
            .creditType(creditType)
            .transactionType(ECreditTransactionType.USAGE)
            .sourceUsage(sourceUsage)
            .sourceUsageId(sourceId)
            .validFrom(null)
            .expiresAt(setExpiresAtHour(expiresAt))
            .userSubscription(userSubscription)
            .build();
    }

    private Date setExpiresAtHour(Date expiresAt) {
        if (isNull(expiresAt)) return null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(expiresAt);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private void sendUserSubscriptionUpdateMessage(User user) {
        sendUserSubscriptionUpdateMessageUsecase.execute(user);
    }
}
