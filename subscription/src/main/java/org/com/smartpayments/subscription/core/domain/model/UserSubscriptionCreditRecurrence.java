package org.com.smartpayments.subscription.core.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_subscription_credit_recurrences")
public class UserSubscriptionCreditRecurrence {
    @EmbeddedId
    private KeyId id;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", insertable = false)
    private Date updatedAt;

    @MapsId("userSubscriptionId")
    @JoinColumn(name = "user_subscription_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private UserSubscription userSubscription;

    @MapsId("creditId")
    @JoinColumn(name = "credit_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Credit credit;

    @Data
    @Builder
    @Embeddable
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KeyId {
        private Long userSubscriptionId;
        private Long creditId;
    }
}
