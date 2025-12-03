package org.com.smartpayments.subscription.core.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.com.smartpayments.subscription.core.domain.enums.EPurchaseChargeStatus;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "purchase_charges")
public class PurchaseCharge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "external_id", unique = true, length = 200)
    private String externalId;

    @Column(name = "total_value", nullable = false)
    private BigDecimal totalValue;

    @Column(name = "status", length = 60, nullable = false)
    private EPurchaseChargeStatus status;

    @Column(name = "payment_url", length = 200, nullable = false)
    private String paymentUrl;

    @Column(name = "payment_date")
    private Date paymentDate;

    @Column(name = "due_date")
    private Date dueDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_id", nullable = false)
    private Purchase purchase;
}
