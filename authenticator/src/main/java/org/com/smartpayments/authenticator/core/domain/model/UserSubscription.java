package org.com.smartpayments.authenticator.core.domain.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.com.smartpayments.authenticator.core.domain.enums.EPlan;
import org.com.smartpayments.authenticator.core.domain.enums.ESubscriptionRecurrence;
import org.com.smartpayments.authenticator.core.domain.enums.ESubscriptionStatus;

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

    @Column(name = "unlimited_email_credits")
    private Boolean unlimitedEmailCredits;

    @Column(name = "email_credits")
    private Integer emailCredits;

    @Column(name = "unlimited_whatsapp_credits")
    private Boolean unlimitedWhatsAppCredits;

    @Column(name = "whatsapp_credits")
    private Integer whatsAppCredits;

    @Column(name = "unlimited_sms_credits")
    private Boolean unlimitedSmsCredits;

    @Column(name = "sms_credits")
    private Integer smsCredits;

    @JoinColumn(name = "user_id")
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private User user;
}
