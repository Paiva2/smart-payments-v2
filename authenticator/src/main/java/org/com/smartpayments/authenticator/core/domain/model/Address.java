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
import lombok.Data;
import lombok.NoArgsConstructor;
import org.com.smartpayments.authenticator.core.domain.enums.EBrState;
import org.com.smartpayments.authenticator.core.domain.enums.ECountry;
import org.com.smartpayments.authenticator.core.ports.out.dto.AddressOutput;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "addresses")
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "street", nullable = false, length = 50)
    private String street;

    @Column(name = "neighborhood", nullable = false, length = 50)
    private String neighborhood;

    @Column(name = "number", nullable = false, length = 15)
    private String number;

    @Column(name = "zipcode", nullable = false, length = 50)
    private String zipcode;

    @Column(name = "complement", length = 300)
    private String complement;

    @Column(name = "city", nullable = false, length = 50)
    private String city;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 50)
    private EBrState state;

    @Enumerated(EnumType.STRING)
    @Column(name = "country", nullable = false, length = 10)
    private ECountry country;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", insertable = false)
    private Date updatedAt;

    @JoinColumn(name = "user_id")
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private User user;

    public AddressOutput toAddressOutput() {
        return AddressOutput.builder()
            .id(this.id)
            .street(this.street)
            .neighborhood(this.neighborhood)
            .number(this.number)
            .zipcode(this.zipcode)
            .complement(this.complement)
            .city(this.city)
            .state(this.state)
            .country(this.country)
            .updatedAt(this.updatedAt)
            .build();
    }
}
