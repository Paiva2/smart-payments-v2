package org.com.smartpayments.authenticator.core.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.com.smartpayments.authenticator.core.domain.enums.EPlan;
import org.com.smartpayments.authenticator.core.domain.enums.ESubscriptionRecurrence;
import org.com.smartpayments.authenticator.core.domain.enums.ESubscriptionStatus;
import org.com.smartpayments.authenticator.core.ports.out.dto.UserSubscriptionOutput;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users_subscriptions")
public class UserSubscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "value", nullable = false)
    private BigDecimal value;

    @Column(name = "next_payment_date", nullable = true)
    private Date nextPaymentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ESubscriptionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "recurrence", nullable = true)
    private ESubscriptionRecurrence recurrence;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan", nullable = true)
    private EPlan plan;

    @Column(name = "email_credits")
    private Integer emailCredits;

    @Column(name = "subscription_email_credits")
    private Integer subscriptionEmailCredits;

    @Column(name = "sms_credits")
    private Integer smsCredits;

    @Column(name = "subscription_sms_credits")
    private Integer subscriptionSmsCredits;

    @Column(name = "whatsapp_credits")
    private Integer whatsAppCredits;

    @Column(name = "subscription_whatsapp_credits")
    private Integer subscriptionWhatsAppCredits;

    @Column(name = "unlimited_email_credits")
    private Boolean unlimitedEmailCredits;

    @Column(name = "unlimited_whatsapp_credits")
    private Boolean unlimitedWhatsAppCredits;

    @Column(name = "unlimited_sms_credits")
    private Boolean unlimitedSmsCredits;

    @JoinColumn(name = "user_id")
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private User user;

    public UserSubscriptionOutput toUserSubscriptionOutput() {
        return UserSubscriptionOutput.builder()
            .value(this.value)
            .nextPaymentDate(this.nextPaymentDate)
            .status(this.status)
            .recurrence(this.recurrence)
            .plan(this.plan)
            .unlimitedEmailCredits(this.unlimitedEmailCredits)
            .emailCredits(this.emailCredits)
            .subscriptionEmailCredits(this.subscriptionEmailCredits)
            .subscriptionSmsCredits(this.subscriptionSmsCredits)
            .subscriptionWhatsAppCredits(this.subscriptionWhatsAppCredits)
            .unlimitedWhatsAppCredits(this.unlimitedWhatsAppCredits)
            .whatsAppCredits(this.whatsAppCredits)
            .unlimitedSmsCredits(this.unlimitedSmsCredits)
            .smsCredits(this.smsCredits)
            .build();
    }
}
