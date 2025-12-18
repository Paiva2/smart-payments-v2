package org.com.smartpayments.subscription.integration.core.domain.usecase.purchaseCharge;

import lombok.SneakyThrows;
import org.com.smartpayments.subscription.core.domain.enums.ECredit;
import org.com.smartpayments.subscription.core.domain.enums.EPlan;
import org.com.smartpayments.subscription.core.domain.enums.EPurchaseChargeStatus;
import org.com.smartpayments.subscription.core.domain.enums.EPurchaseType;
import org.com.smartpayments.subscription.core.domain.model.Credit;
import org.com.smartpayments.subscription.core.domain.model.Plan;
import org.com.smartpayments.subscription.core.domain.model.Purchase;
import org.com.smartpayments.subscription.core.domain.model.PurchaseCharge;
import org.com.smartpayments.subscription.core.domain.model.User;
import org.com.smartpayments.subscription.core.ports.in.dto.AsyncMessageInput;
import org.com.smartpayments.subscription.core.ports.in.dto.CreatePurchaseChargeInput;
import org.com.smartpayments.subscription.infra.persistence.repository.CreditRepository;
import org.com.smartpayments.subscription.infra.persistence.repository.PlanRepository;
import org.com.smartpayments.subscription.infra.persistence.repository.PurchaseChargeRepository;
import org.com.smartpayments.subscription.integration.fixtures.bases.IntegrationTestBase;
import org.com.smartpayments.subscription.integration.utils.PurchaseUtils;
import org.com.smartpayments.subscription.integration.utils.UserUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class CreatePurchaseChargeUsecaseTest extends IntegrationTestBase {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private PurchaseUtils purchaseUtils;

    @Autowired
    private CreditRepository creditRepository;

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private PurchaseChargeRepository purchaseChargeRepository;

    @Autowired
    private UserUtils userUtils;

    @Value("${spring.kafka.topics.new-purchase-charge}")
    private String newPurchaseChargeTopic;

    private User user;

    private Credit creditPurchased;

    private Plan planPurchased;

    @BeforeEach
    public void setup() {
        this.creditPurchased = creditRepository.findByTypeAndActiveIsTrue(ECredit.EMAIL).get();
        this.planPurchased = planRepository.findByType(EPlan.STARTER).get();


        this.user = userUtils.createUser(true, true);
    }

    @BeforeEach
    public void beforeEach() {

    }

    @Test
    @SneakyThrows
    @DisplayName("Should create credit purchase charge when receiving async message")
    public void createCreditPurchaseCharge() {
        Purchase purchase = purchaseUtils.createPurchase(EPurchaseType.CREDIT, this.user, null, creditPurchased);

        AsyncMessageInput<CreatePurchaseChargeInput> input = input(purchase);
        kafkaTemplate.send(newPurchaseChargeTopic, purchase.getId().toString(), objectMapper.writeValueAsString(input));

        await()
            .atMost(15, SECONDS)
            .untilAsserted(() -> {
                Optional<PurchaseCharge> purchaseChargeCreated = purchaseChargeRepository.findByExternalId(input.getData().getExternalChargeId());

                assertTrue(purchaseChargeCreated.isPresent());
                assertEquals(input.getData().getExternalChargeId(), purchaseChargeCreated.get().getExternalId());
                assertEquals(input.getData().getValue(), purchaseChargeCreated.get().getTotalValue());
                assertEquals(input.getData().getExternalChargeId(), purchaseChargeCreated.get().getExternalId());
                assertEquals(EPurchaseChargeStatus.PENDING, purchaseChargeCreated.get().getStatus());
                assertEquals(purchase.getId(), purchaseChargeCreated.get().getPurchase().getId());
                assertNotNull(purchaseChargeCreated.get().getPaymentUrl());
                assertNull(purchaseChargeCreated.get().getPaymentDate());
            });
    }

    @Test
    @SneakyThrows
    @DisplayName("Should create subscription purchase charges when receiving async message")
    public void createSubscriptionPurchaseCharges() {
        Purchase purchase = purchaseUtils.createPurchase(EPurchaseType.SUBSCRIPTION, this.user, planPurchased, null);

        AsyncMessageInput<CreatePurchaseChargeInput> input = input(purchase);
        input.getData().setExternalChargeId("1st_charge_id");
        kafkaTemplate.send(newPurchaseChargeTopic, purchase.getId().toString(), objectMapper.writeValueAsString(input)); // 1st charge

        input.getData().setExternalChargeId("2st_charge_id");
        kafkaTemplate.send(newPurchaseChargeTopic, purchase.getId().toString(), objectMapper.writeValueAsString(input)); // 2st charge

        await()
            .atMost(25, SECONDS)
            .untilAsserted(() -> {
                List<PurchaseCharge> purchaseChargeCreated = purchaseChargeRepository.findByPurchaseId(purchase.getId());

                assertFalse(purchaseChargeCreated.isEmpty());
                assertEquals(2, purchaseChargeCreated.size());

                assertEquals(purchase.getId(), purchaseChargeCreated.getFirst().getPurchase().getId());
                assertEquals("1st_charge_id", purchaseChargeCreated.getFirst().getExternalId());
                assertEquals(EPurchaseChargeStatus.PENDING, purchaseChargeCreated.getFirst().getStatus());
                assertEquals(input.getData().getValue(), purchaseChargeCreated.getFirst().getTotalValue());

                assertEquals(purchase.getId(), purchaseChargeCreated.getLast().getPurchase().getId());
                assertEquals("2st_charge_id", purchaseChargeCreated.getLast().getExternalId());
                assertEquals(EPurchaseChargeStatus.PENDING, purchaseChargeCreated.getLast().getStatus());
                assertEquals(input.getData().getValue(), purchaseChargeCreated.getLast().getTotalValue());
            });
    }

    @Test
    @SneakyThrows
    @DisplayName("Should NOT create subscription purchase charges when receiving async message if already exists")
    public void createSubscriptionPurchaseChargesAlreadyExisting() {
        Purchase purchase = purchaseUtils.createPurchase(EPurchaseType.SUBSCRIPTION, this.user, planPurchased, null);

        AsyncMessageInput<CreatePurchaseChargeInput> input = input(purchase);
        kafkaTemplate.send(newPurchaseChargeTopic, purchase.getId().toString(), objectMapper.writeValueAsString(input));
        kafkaTemplate.send(newPurchaseChargeTopic, purchase.getId().toString(), objectMapper.writeValueAsString(input));

        await()
            .atMost(25, SECONDS)
            .untilAsserted(() -> {
                List<PurchaseCharge> purchaseChargeCreated = purchaseChargeRepository.findByPurchaseId(purchase.getId());

                assertFalse(purchaseChargeCreated.isEmpty());
                assertEquals(1, purchaseChargeCreated.size());

                assertEquals(purchase.getId(), purchaseChargeCreated.getFirst().getPurchase().getId());
                assertEquals(input.getData().getExternalChargeId(), purchaseChargeCreated.getFirst().getExternalId());
            });
    }

    private AsyncMessageInput<CreatePurchaseChargeInput> input(Purchase purchase) {
        CreatePurchaseChargeInput purchaseChargeInput = CreatePurchaseChargeInput.builder()
            .externalChargeId("any_id")
            .externalPurchaseId(purchase.getExternalId())
            .value(purchase.getTotalValue())
            .description("any_description")
            .billingType(purchase.getPaymentMethod().name())
            .pixTransaction(null)
            .pixQrCodeId(null)
            .status(EPurchaseChargeStatus.PENDING)
            .dueDate(new Date())
            .paymentDate(null)
            .invoiceUrl("any_url")
            .bankSlipUrl("any_url")
            .build();

        return new AsyncMessageInput<>("any_hash", new Date(), "SUBSCRIPTION", purchaseChargeInput);
    }
}
