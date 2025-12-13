package org.com.smartpayments.subscription.core.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.com.smartpayments.subscription.core.domain.enums.EPlan;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "plans")
public class Plan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", unique = true, nullable = false, length = 50)
    private EPlan type;

    @Column(name = "value", nullable = false)
    private BigDecimal value;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "unlimited_email_credits", nullable = false)
    private Boolean unlimitedEmailCredits;

    @Column(name = "email_credits_quantity", nullable = false)
    private Integer emailCreditsQuantity;

    @Column(name = "unlimited_whats_app_credits", nullable = false)
    private Boolean unlimitedWhatsAppCredits;

    @Column(name = "whats_app_credits_quantity", nullable = false)
    private Integer whatsAppCreditsQuantity;

    @Column(name = "unlimited_sms_credits", nullable = false)
    private Boolean unlimitedSmsCredits;

    @Column(name = "sms_credits_quantity", nullable = false)
    private Integer smsCreditsQuantity;

    @Column(name = "description")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", insertable = false)
    private Date updatedAt;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "plan")
    private List<PurchaseItem> purchaseItems;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "plan")
    private List<UserSubscription> subscriptions;
}
