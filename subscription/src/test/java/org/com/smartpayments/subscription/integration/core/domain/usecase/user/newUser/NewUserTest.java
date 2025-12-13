package org.com.smartpayments.subscription.integration.core.domain.usecase.user.newUser;

import com.github.javafaker.Faker;
import lombok.SneakyThrows;
import org.com.smartpayments.subscription.core.domain.enums.EBrState;
import org.com.smartpayments.subscription.core.domain.enums.ECountry;
import org.com.smartpayments.subscription.core.domain.enums.EPlan;
import org.com.smartpayments.subscription.core.domain.enums.ESubscriptionStatus;
import org.com.smartpayments.subscription.core.domain.enums.EUserType;
import org.com.smartpayments.subscription.core.domain.model.User;
import org.com.smartpayments.subscription.core.domain.model.UserSubscription;
import org.com.smartpayments.subscription.core.domain.usecase.user.newUser.NewUserUsecase;
import org.com.smartpayments.subscription.core.ports.in.dto.AsyncMessageInput;
import org.com.smartpayments.subscription.core.ports.in.dto.AsyncNewUserInput;
import org.com.smartpayments.subscription.infra.persistence.repository.UserRepository;
import org.com.smartpayments.subscription.infra.persistence.repository.UserSubscriptionRepository;
import org.com.smartpayments.subscription.integration.fixtures.bases.IntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.com.smartpayments.subscription.integration.constants.TestConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class NewUserTest extends IntegrationTestBase {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @MockitoSpyBean
    private NewUserUsecase newUserUsecase;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSubscriptionRepository userSubscriptionRepository;

    @Value("${spring.kafka.topics.new-user}")
    private String newUserTopic;

    private final static long USER_ID = 1L;

    @Test
    @SneakyThrows
    @DisplayName("Should create user when receive async message")
    public void createUser() {
        AsyncMessageInput<AsyncNewUserInput> input = input(USER_ID);
        kafkaTemplate.send(newUserTopic, String.valueOf(USER_ID), objectMapper.writeValueAsString(input));

        await()
            .atMost(20, SECONDS)
            .untilAsserted(() ->
                verify(newUserUsecase, times(1)).execute(any(AsyncNewUserInput.class))
            );

        await()
            .atMost(20, SECONDS)
            .untilAsserted(() -> {
                Optional<User> userCreated = userRepository.findById(USER_ID);
                assertTrue(userCreated.isPresent());
                assertEquals(userCreated.get().getEmail(), input.getData().getEmail());

                Optional<UserSubscription> userSubscription = userSubscriptionRepository.findByUserIdWithPlan(userCreated.get().getId());

                assertTrue(userSubscription.isPresent());
                assertEquals(ESubscriptionStatus.ACTIVE, userSubscription.get().getStatus());
                assertEquals(new BigDecimal("0.00"), userSubscription.get().getValue());
                assertEquals(EPlan.FREE, userSubscription.get().getPlan().getType());
            });
    }

    @SneakyThrows
    private AsyncMessageInput<AsyncNewUserInput> input(Long userId) {
        final Faker faker = new Faker();

        AsyncNewUserInput userInput = AsyncNewUserInput.builder()
            .id(userId)
            .firstName(faker.name().fullName())
            .lastName(faker.name().lastName())
            .email(faker.internet().emailAddress().toLowerCase(Locale.ROOT))
            .cpfCnpj(RANDOM_VALID_CPF)
            .type(EUserType.NATURAL)
            .ddi("+55")
            .phone(RANDOM_VALID_PHONE_NUMBER)
            .birthdate(new SimpleDateFormat("dd-MM-yyyy").parse(RANDOM_VALID_BIRTHDATE))
            .active(true)
            .emailConfirmedAt(new Date())
            .createdAt(new Date())
            .updatedAt(null)
            .address(AsyncNewUserInput.AsyncAddressOutput.builder()
                .street(faker.address().streetName())
                .neighborhood("test_neighborhood")
                .number(faker.address().streetAddressNumber())
                .zipcode(faker.numerify("#####-###"))
                .city(faker.address().city())
                .state(EBrState.SP)
                .complement("test_complement")
                .country(ECountry.BR)
                .build()
            ).build();

        return new AsyncMessageInput<>("any_hash", new Date(), "AUTHENTICATOR", userInput);
    }
}
