package org.com.smartpayments.subscription.core.domain.model;

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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.com.smartpayments.subscription.core.domain.enums.EPaymentMethod;
import org.com.smartpayments.subscription.core.domain.enums.EPurchaseStatus;
import org.com.smartpayments.subscription.core.domain.enums.EPurchaseType;
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
@Table(name = "purchases")
public class Purchase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "external_id", unique = true, length = 200)
    private String externalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private EPaymentMethod paymentMethod;

    @Column(name = "total_value", nullable = false)
    private BigDecimal totalValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private EPurchaseStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "purchase_type", nullable = false, length = 20)
    private EPurchaseType purchaseType;

    @Column(name = "installments", nullable = false)
    private Integer installments;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", insertable = false)
    private Date updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "purchase", cascade = CascadeType.ALL)
    private List<PurchaseItem> items;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "purchase")
    private List<PurchaseCharge> charges;
}
