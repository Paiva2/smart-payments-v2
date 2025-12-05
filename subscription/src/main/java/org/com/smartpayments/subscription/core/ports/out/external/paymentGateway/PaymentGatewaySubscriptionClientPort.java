package org.com.smartpayments.subscription.core.ports.out.external.paymentGateway;

import org.com.smartpayments.subscription.core.domain.model.User;
import org.com.smartpayments.subscription.core.ports.out.external.dto.CreateSubscriptionOutput;
import org.com.smartpayments.subscription.core.ports.out.external.dto.DeleteSubscriptionOutput;
import org.com.smartpayments.subscription.core.ports.out.external.dto.GetSubscriptionChargesOutput;
import org.com.smartpayments.subscription.core.ports.out.external.dto.NewSubscriptionClientInput;

public interface PaymentGatewaySubscriptionClientPort {
    CreateSubscriptionOutput createSubscription(User user, NewSubscriptionClientInput input);

    GetSubscriptionChargesOutput getSubscriptionCharges(User user, String subscriptionId);

    DeleteSubscriptionOutput deleteSubscription(User user, String subscriptionId);
}
