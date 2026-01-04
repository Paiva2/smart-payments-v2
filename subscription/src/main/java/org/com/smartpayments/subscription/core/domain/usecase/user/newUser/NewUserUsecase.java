package org.com.smartpayments.subscription.core.domain.usecase.user.newUser;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.smartpayments.subscription.core.common.exception.PlanNotFoundException;
import org.com.smartpayments.subscription.core.domain.enums.ECountry;
import org.com.smartpayments.subscription.core.domain.enums.ECredit;
import org.com.smartpayments.subscription.core.domain.enums.EPlan;
import org.com.smartpayments.subscription.core.domain.enums.ESubscriptionStatus;
import org.com.smartpayments.subscription.core.domain.model.*;
import org.com.smartpayments.subscription.core.ports.in.UsecaseVoidPort;
import org.com.smartpayments.subscription.core.ports.in.dto.AsyncNewUserInput;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.PlanDataProviderPort;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.UserDataProviderPort;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.UserSubscriptionCreditHistoryDataProviderPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.nonNull;
import static org.com.smartpayments.subscription.core.domain.enums.ECreditTransactionType.SAMPLE_GRANT;

@Slf4j
@Service
@AllArgsConstructor
public class NewUserUsecase implements UsecaseVoidPort<AsyncNewUserInput> {
    private final int DEFAULT_GIVEN_CREDITS_QUANTITY = 5;

    private final UserDataProviderPort userDataProviderPort;
    private final PlanDataProviderPort planDataProviderPort;
    private final UserSubscriptionCreditHistoryDataProviderPort userSubscriptionCreditHistoryDataProviderPort;

    @Transactional
    public void execute(AsyncNewUserInput input) {
        Optional<User> userExists = userDataProviderPort.findById(input.getId());

        if (userExists.isPresent()) {
            log.warn("[NewUserUsecase#execute] - New user message but user already exists, message discarded: {}", input);
            return;
        }

        User user = fillUser(input);
        user = userDataProviderPort.persist(user);
        persistSampleCredits(user.getSubscription());
        log.info("[NewUserUsecase#execute] - New user created: {}", user);
    }

    private User fillUser(AsyncNewUserInput input) {
        User user = User.builder()
            .id(input.getId())
            .userPaymentGatewayExternalId(null)
            .firstName(input.getFirstName())
            .lastName(input.getLastName())
            .email(input.getEmail())
            .cpfCnpj(input.getCpfCnpj())
            .type(input.getType())
            .ddi(input.getDdi())
            .phone(input.getPhone())
            .birthdate(input.getBirthdate())
            .active(input.getActive() && nonNull(input.getEmailConfirmedAt()))
            .build();

        Address address = fillAddress(input, user);
        user.setAddress(address);

        UserSubscription userSubscription = fillUserSubscription(user);
        user.setSubscription(userSubscription);

        return user;
    }

    private Address fillAddress(AsyncNewUserInput input, User user) {
        return Address.builder()
            .street(input.getAddress().getStreet())
            .neighborhood(input.getAddress().getNeighborhood())
            .number(input.getAddress().getNumber())
            .zipcode(input.getAddress().getZipcode())
            .complement(input.getAddress().getComplement())
            .city(input.getAddress().getCity())
            .state(input.getAddress().getState())
            .country(ECountry.BR)
            .user(user)
            .build();
    }

    private UserSubscription fillUserSubscription(User user) {
        return UserSubscription.builder()
            .value(BigDecimal.valueOf(0))
            .status(ESubscriptionStatus.ACTIVE)
            .nextPaymentDate(null)
            .recurrence(null)
            .user(user)
            .expiredAt(null)
            .externalSubscriptionId(null)
            .plan(findFreePlan())
            .unlimitedEmailCredits(false)
            .unlimitedWhatsAppCredits(false)
            .unlimitedSmsCredits(false)
            .build();
    }

    private Plan findFreePlan() {
        return planDataProviderPort.findByType(EPlan.FREE).orElseThrow(() -> new PlanNotFoundException("FREE plan not found!"));
    }

    private void persistSampleCredits(UserSubscription userSubscription) {
        List<UserSubscriptionCreditHistory> creditsGiven = new ArrayList<>() {{
            add(fillCreditHistory(ECredit.EMAIL, userSubscription));
        }};

        userSubscriptionCreditHistoryDataProviderPort.persistAll(creditsGiven);
    }

    private UserSubscriptionCreditHistory fillCreditHistory(ECredit credit, UserSubscription userSubscription) {
        return UserSubscriptionCreditHistory.builder()
            .amount(DEFAULT_GIVEN_CREDITS_QUANTITY)
            .creditType(credit)
            .transactionType(SAMPLE_GRANT)
            .userSubscription(userSubscription)
            .build();
    }
}
