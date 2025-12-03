package org.com.smartpayments.subscription.core.domain.core.ports.out.external;

import org.com.smartpayments.subscription.core.domain.core.ports.out.external.dto.CreateSubscriptionOutput;
import org.com.smartpayments.subscription.core.domain.core.ports.out.external.dto.DeleteSubscriptionOutput;
import org.com.smartpayments.subscription.core.domain.core.ports.out.external.dto.GetSubscriptionChargesOutput;
import org.com.smartpayments.subscription.core.domain.core.ports.out.external.dto.NewSubscriptionClientInput;
import org.com.smartpayments.subscription.core.domain.model.User;

public interface PaymentGatewaySubscriptionClientPort {
    CreateSubscriptionOutput createSubscription(User user, NewSubscriptionClientInput input);

    GetSubscriptionChargesOutput getSubscriptionCharges(User user, String subscriptionId);

    DeleteSubscriptionOutput deleteSubscription(User user, String subscriptionId);
}
