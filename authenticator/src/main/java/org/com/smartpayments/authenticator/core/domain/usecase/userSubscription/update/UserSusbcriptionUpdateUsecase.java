package org.com.smartpayments.authenticator.core.domain.usecase.userSubscription.update;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.smartpayments.authenticator.core.common.exception.UserSubscriptionNotFoundException;
import org.com.smartpayments.authenticator.core.domain.model.UserSubscription;
import org.com.smartpayments.authenticator.core.ports.in.UsecaseVoidPort;
import org.com.smartpayments.authenticator.core.ports.in.dto.UserSubscriptionUpdateInput;
import org.com.smartpayments.authenticator.core.ports.out.dataProvider.UserSubscriptionDataProviderPort;
import org.com.smartpayments.authenticator.core.ports.out.utils.DateUtilsPort;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.Date;

import static java.util.Objects.isNull;
import static org.com.smartpayments.authenticator.core.common.constants.Constants.USER_PROFILE_CACHE_LABEL;

@Slf4j
@Service
@AllArgsConstructor
public class UserSusbcriptionUpdateUsecase implements UsecaseVoidPort<UserSubscriptionUpdateInput> {
    private final UserSubscriptionDataProviderPort userSubscriptionDataProviderPort;

    private final DateUtilsPort dateUtilsPort;

    @Override
    @CacheEvict(value = USER_PROFILE_CACHE_LABEL, key = "#input.userId")
    public void execute(UserSubscriptionUpdateInput input) {
        UserSubscription userSubscription = findUserSubscription(input.getUserId());
        updateUserSubscription(userSubscription, input);
        persistUserSubscription(userSubscription);
    }

    private UserSubscription findUserSubscription(Long userId) {
        return userSubscriptionDataProviderPort.findByUserId(userId)
            .orElseThrow(UserSubscriptionNotFoundException::new);
    }

    private void updateUserSubscription(UserSubscription userSubscription, UserSubscriptionUpdateInput input) {
        userSubscription.setPlan(input.getPlan());
        userSubscription.setNextPaymentDate(convertNextPaymentDate(input.getNextPaymentDate()));
        userSubscription.setStatus(input.getStatus());
        userSubscription.setRecurrence(input.getRecurrence());
        userSubscription.setValue(input.getValue());
        userSubscription.setUnlimitedEmailCredits(input.getUnlimitedEmailCredits());
        userSubscription.setEmailCredits(input.getEmailCredits());
        userSubscription.setUnlimitedWhatsAppCredits(input.getUnlimitedWhatsAppCredits());
        userSubscription.setWhatsAppCredits(input.getWhatsAppCredits());
        userSubscription.setSmsCredits(input.getSmsCredits());
        userSubscription.setUnlimitedSmsCredits(input.getUnlimitedSmsCredits());
    }

    private void persistUserSubscription(UserSubscription userSubscription) {
        userSubscriptionDataProviderPort.persist(userSubscription);
    }

    private Date convertNextPaymentDate(String date) {
        if (isNull(date)) return null;

        try {
            return dateUtilsPort.convertDate(date, "yyyy-MM-dd HH:mm:ss.SSS");
        } catch (Exception e) {
            log.error("[UserSusbcriptionUpdateUsecase#convertNextPaymentDate] - Error while converting next payment date. Error: {}", e.getMessage());
            return null;
        }
    }
}
