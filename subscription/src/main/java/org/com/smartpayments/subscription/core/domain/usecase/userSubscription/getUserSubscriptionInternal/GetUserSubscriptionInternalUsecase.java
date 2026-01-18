package org.com.smartpayments.subscription.core.domain.usecase.userSubscription.getUserSubscriptionInternal;

import lombok.AllArgsConstructor;
import org.com.smartpayments.subscription.core.common.exception.UserNotFoundException;
import org.com.smartpayments.subscription.core.domain.model.User;
import org.com.smartpayments.subscription.core.domain.model.UserSubscription;
import org.com.smartpayments.subscription.core.ports.in.UsecasePort;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.UserDataProviderPort;
import org.com.smartpayments.subscription.core.ports.out.dto.GetUserSubscriptionInternalOutput;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GetUserSubscriptionInternalUsecase implements UsecasePort<Long, GetUserSubscriptionInternalOutput> {
    private final UserDataProviderPort userDataProviderPort;

    @Override
    public GetUserSubscriptionInternalOutput execute(Long userId) {
        User user = userDataProviderPort.findActiveWithSubscriptionById(userId)
            .orElseThrow(UserNotFoundException::new);
        UserSubscription userSubscription = user.getSubscription();

        return GetUserSubscriptionInternalOutput.builder()
            .id(userSubscription.getId())
            .firstName(user.getFirstName())
            .email(user.getEmail())
            .phoneNumber(user.getPhone())
            .plan(userSubscription.getPlan().getType().name())
            .unlimitedEmailCredits(userSubscription.getUnlimitedEmailCredits())
            .unlimitedSmsCredits(userSubscription.getUnlimitedSmsCredits())
            .unlimitedWhatsAppCredits(userSubscription.getUnlimitedWhatsAppCredits())
            .build();
    }
}
