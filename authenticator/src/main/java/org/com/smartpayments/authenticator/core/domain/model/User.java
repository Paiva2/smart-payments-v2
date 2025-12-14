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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.com.smartpayments.authenticator.core.common.exception.GenericInvalidBirthdateException;
import org.com.smartpayments.authenticator.core.common.exception.GenericPhoneInvalidException;
import org.com.smartpayments.authenticator.core.domain.enums.EUserType;
import org.com.smartpayments.authenticator.core.ports.out.dto.UserProfileOutput;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.springframework.util.ObjectUtils.isEmpty;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true, length = 50)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

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

    @Column(name = "password_token", unique = true, length = 180)
    private String passwordToken;

    @Column(name = "password_token_sent_at")
    private Date passwordTokenSentAt;

    @Column(name = "email_token", unique = true, length = 180)
    private String emailToken;

    @Column(name = "email_token_sent_at")
    private Date emailTokenSentAt;

    @Column(name = "email_confirmed_at")
    private Date emailConfirmedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Address address;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private UserSubscription userSubscription;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserRole> userRoles;

    @Transient
    private String profilePictureUrl;

    @PreUpdate
    @PrePersist
    private void makeValidations() {
        this.validatePhone();
        this.validateBirthdate();
        this.formatCpfCnpj();
    }

    private void validateBirthdate() {
        if (isNull(this.birthdate)) {
            throw new GenericInvalidBirthdateException("Birthdate can't be null!");
        }

        Calendar calendar = GregorianCalendar.getInstance();
        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) - 18);

        if (!calendar.getTime().after(this.birthdate)) {
            throw new GenericInvalidBirthdateException("User must have at least 18 years old!");
        }
    }

    private void formatCpfCnpj() {
        this.cpfCnpj = this.cpfCnpj.replaceAll("\\D", "");
    }

    private void validatePhone() {
        if (isEmpty(this.phone)) return;

        final String phoneFormatted = this.phone.replaceAll("[^0-9]", "");

        if (nonNull(this.ddi) && Objects.equals(this.ddi, "+55")) {
            if (phoneFormatted.length() != 11) {
                throw new GenericPhoneInvalidException("Phone must have 11 digits!");
            }
        }
    }

    public UserProfileOutput toProfileOutput() {
        return UserProfileOutput.builder()
            .id(this.id)
            .firstName(this.firstName)
            .lastName(this.lastName)
            .email(this.email)
            .cpfCnpj(this.cpfCnpj)
            .type(this.type)
            .profilePictureUrl(this.profilePictureUrl)
            .ddi(this.ddi)
            .phone(this.phone)
            .birthdate(this.birthdate)
            .active(this.active)
            .createdAt(this.createdAt)
            .updatedAt(this.updatedAt)
            .subscription(isEmpty(this.userSubscription) ? null : this.userSubscription.toUserSubscriptionOutput())
            .address(isEmpty(this.address) ? null : this.address.toAddressOutput())
            .roles(isEmpty(this.userRoles) ? null : this.userRoles.stream().map(UserRole::getRole).map(Role::toRoleOutput).toList())
            .emailConfirmedAt(this.emailConfirmedAt)
            .build();
    }
}
