package org.com.smartpayments.subscription.core.domain.usecase.userSubscription.cancelSubscription.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.com.smartpayments.subscription.core.domain.model.User;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionCancelledEvent {
    private User user;
}
