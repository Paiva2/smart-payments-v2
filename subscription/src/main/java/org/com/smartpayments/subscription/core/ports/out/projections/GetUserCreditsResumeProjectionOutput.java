package org.com.smartpayments.subscription.core.ports.out.projections;

public interface GetUserCreditsResumeProjectionOutput {
    Integer getSmsSubscriptionCredits();

    Integer getSmsCredits();

    Integer getEmailCredits();

    Integer getEmailSubscriptionCredits();

    Integer getWhatsAppCredits();

    Integer getWhatsAppSubscriptionCredits();
}
