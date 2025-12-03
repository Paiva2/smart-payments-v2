package org.com.smartpayments.subscription.core.domain.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.com.smartpayments.subscription.core.domain.enums.EUserType;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "user_payment_gateway_external_id", unique = true, nullable = true, length = 200)
    private String userPaymentGatewayExternalId;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true, length = 50)
    private String email;

    @Column(name = "cpf_cnpj", nullable = false, unique = true, length = 20)
    private String cpfCnpj;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 15)
    private EUserType type;

    @Column(name = "ddi", nullable = false, length = 5)
    private String ddi;

    @Column(name = "phone", length = 30)
    private String phone;

    @Column(name = "birthdate", nullable = false)
    private Date birthdate;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Address address;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    private List<Purchase> purchases;
}
