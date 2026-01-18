package org.com.smartpayments.subscription.core.domain.model;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.com.smartpayments.subscription.core.ports.out.dto.ConsumeUserCreditsInternalOutput;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;

import java.util.Date;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "credit_consumption_idempotency")
public class CreditConsumptionIdempotency {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "idempotency_key")
    private String idempotencyKey;

    @Type(JsonBinaryType.class)
    @Column(name = "data", columnDefinition = "jsonb")
    private ConsumeUserCreditsInternalOutput data;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Date createdAt;
}
