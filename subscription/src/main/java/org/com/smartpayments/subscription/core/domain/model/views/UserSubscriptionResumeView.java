package org.com.smartpayments.subscription.core.domain.model.views;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Entity
@Immutable
@Table(name = "vi_user_subscription_resume")
public class UserSubscriptionResumeView {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "value")
    private BigDecimal value;

    @Column(name = "next_payment_date")
    private Date nextPaymentDate;

    @Column(name = "plan")
    private String plan;

    @Column(name = "recurrence")
    private String recurrence;

    @Column(name = "email_credits")
    private Integer emailCredits;

    @Column(name = "sms_credits")
    private Integer smsCredits;

    @Column(name = "whatsapp_credits")
    private Integer whatsappCredits;

    @Column(name = "unlimited_email_credits")
    private Boolean unlimitedEmailCredits;

    @Column(name = "unlimited_sms_credits")
    private Boolean unlimitedSmsCredits;

    @Column(name = "unlimited_whatsapp_credits")
    private Boolean unlimitedWhatsappCredits;
}
