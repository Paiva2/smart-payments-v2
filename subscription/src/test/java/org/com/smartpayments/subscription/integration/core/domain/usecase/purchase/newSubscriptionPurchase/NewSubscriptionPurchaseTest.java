package org.com.smartpayments.subscription.integration.core.domain.usecase.purchase.newSubscriptionPurchase;


import org.com.smartpayments.subscription.core.domain.enums.EPaymentMethod;
import org.com.smartpayments.subscription.core.domain.enums.EPlan;
import org.com.smartpayments.subscription.core.domain.enums.EPurchaseStatus;
import org.com.smartpayments.subscription.core.domain.enums.EPurchaseType;
import org.com.smartpayments.subscription.core.domain.model.Plan;
import org.com.smartpayments.subscription.core.domain.model.Purchase;
import org.com.smartpayments.subscription.core.domain.model.PurchaseItem;
import org.com.smartpayments.subscription.core.domain.model.User;
import org.com.smartpayments.subscription.core.ports.in.dto.NewSubscriptionPurchaseInput;
import org.com.smartpayments.subscription.core.ports.out.external.paymentGateway.PaymentGatewayUserClientPort;
import org.com.smartpayments.subscription.infra.persistence.repository.PlanRepository;
import org.com.smartpayments.subscription.infra.persistence.repository.PurchaseItemRepository;
import org.com.smartpayments.subscription.infra.persistence.repository.PurchaseRepository;
import org.com.smartpayments.subscription.infra.persistence.repository.UserRepository;
import org.com.smartpayments.subscription.integration.fixtures.bases.IntegrationTestBase;
import org.com.smartpayments.subscription.integration.fixtures.wiremock.AuthenticatorStubs;
import org.com.smartpayments.subscription.integration.fixtures.wiremock.PaymentGatewayStubs;
import org.com.smartpayments.subscription.integration.utils.UserUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.com.smartpayments.subscription.integration.constants.TestConstants.RANDOM_VALID_CPF2;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NewSubscriptionPurchaseTest extends IntegrationTestBase {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserUtils userUtils;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlanRepository planRepository;

    @MockitoSpyBean
    private PaymentGatewayUserClientPort paymentGatewayUserClientPort;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private PurchaseItemRepository purchaseItemRepository;

    @Value("${server.api-suffix}")
    private String apiSuffix;

    private String authToken;

    private Plan planPurchased;

    @BeforeEach
    public void setUp() throws Exception {
        planPurchased = planRepository.findByType(EPlan.STARTER).get();

        User user = userUtils.createUser(true, true);
        authToken = userUtils.generateAuthToken(user.getId());

        AuthenticatorStubs.mockAuthenticateUser(wireMockServer, objectMapper, user);

        PaymentGatewayStubs.mockCreateUser(wireMockServer, objectMapper);
    }

    @Test
    @DisplayName("Should create a subscription purchase with user already having payment gateway register")
    public void newSubscriptionPurchase() throws Exception {
        String subscriptionId = "any_subscription_id" + UUID.randomUUID();
        PaymentGatewayStubs.mockCreateSubscription(wireMockServer, objectMapper, subscriptionId);
        PaymentGatewayStubs.mockListSubscriptionCharges(wireMockServer, objectMapper);

        NewSubscriptionPurchaseInput input = input();

        mockMvc.perform(post(apiSuffix + "/purchase/subscription")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + authToken)
                .content(objectMapper.writeValueAsString(input))
            ).andExpect(jsonPath("$.paymentUrl").isNotEmpty())
            .andExpect(status().isOk());


        verify(paymentGatewayUserClientPort, times(0)).newUserClient(any());

        Optional<Purchase> purchase = purchaseRepository.findByExternalId(subscriptionId);

        assertTrue(purchase.isPresent());
        assertEquals(subscriptionId, purchase.get().getExternalId());
        assertEquals(EPurchaseStatus.PENDING, purchase.get().getStatus());
        assertEquals(EPurchaseType.SUBSCRIPTION, purchase.get().getPurchaseType());
        assertEquals(0, purchase.get().getInstallments());

        List<PurchaseItem> purchaseItems = purchaseItemRepository.findByPurchaseId(purchase.get().getId());

        assertFalse(purchaseItems.isEmpty());
        assertEquals(purchaseItems.get(0).getPurchase().getId(), purchase.get().getId());
        assertEquals(purchaseItems.get(0).getValue(), planPurchased.getValue());
        assertNull(purchaseItems.get(0).getCredit());
    }

    @Test
    @DisplayName("Should create a subscription purchase with user not having payment gateway register")
    public void newSubscriptionPurchaseAndCreateUser() throws Exception {
        String subscriptionId = "any_subscription_id" + UUID.randomUUID();
        PaymentGatewayStubs.mockCreateSubscription(wireMockServer, objectMapper, subscriptionId);
        PaymentGatewayStubs.mockListSubscriptionCharges(wireMockServer, objectMapper);

        NewSubscriptionPurchaseInput input = input();
        User otherUser = userUtils.createUser(300L, RANDOM_VALID_CPF2, true, false);
        String authToken = userUtils.generateAuthToken(otherUser.getId());

        mockMvc.perform(post(apiSuffix + "/purchase/subscription")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + authToken)
                .content(objectMapper.writeValueAsString(input))
            ).andExpect(jsonPath("$.paymentUrl").isNotEmpty())
            .andExpect(status().isOk());

        verify(paymentGatewayUserClientPort, times(1)).newUserClient(any());

        Optional<User> userAuthorized = userRepository.findByIdAndActiveIsTrue(otherUser.getId());

        assertTrue(userAuthorized.isPresent());
        assertNotNull(userAuthorized.get().getUserPaymentGatewayExternalId());

        Optional<Purchase> purchase = purchaseRepository.findByExternalId(subscriptionId);

        assertTrue(purchase.isPresent());
        assertEquals(subscriptionId, purchase.get().getExternalId());
        assertEquals(EPurchaseStatus.PENDING, purchase.get().getStatus());
        assertEquals(EPurchaseType.SUBSCRIPTION, purchase.get().getPurchaseType());
        assertEquals(0, purchase.get().getInstallments());

        List<PurchaseItem> purchaseItems = purchaseItemRepository.findByPurchaseId(purchase.get().getId());

        assertFalse(purchaseItems.isEmpty());
        assertEquals(purchaseItems.get(0).getPurchase().getId(), purchase.get().getId());
        assertEquals(purchaseItems.get(0).getValue(), planPurchased.getValue());
        assertNull(purchaseItems.get(0).getCredit());
    }

    @Test
    @DisplayName("Should not create a subscription if purchase items is empty")
    public void newSubscriptionWithoutPurchaseItems() throws Exception {
        NewSubscriptionPurchaseInput input = input();
        input.setPurchaseItems(null);

        mockMvc.perform(post(apiSuffix + "/purchase/subscription")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + authToken)
            .content(objectMapper.writeValueAsString(input))
        ).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should not create a subscription if installments is invalid")
    public void newSubscriptionWithInvalidInstallments() throws Exception {
        NewSubscriptionPurchaseInput input = input();
        input.setInstallments(-1);

        mockMvc.perform(post(apiSuffix + "/purchase/subscription")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + authToken)
            .content(objectMapper.writeValueAsString(input))
        ).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should not create a subscription if provided installments are not allowed")
    public void newSubscriptionWithInvalidInstallmentsValue() throws Exception {
        NewSubscriptionPurchaseInput input = input();
        input.setInstallments(2000);

        mockMvc.perform(post(apiSuffix + "/purchase/subscription")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + authToken)
                .content(objectMapper.writeValueAsString(input))
            ).andExpect(jsonPath("$.message").value("Invalid stallments. Maximum installments allowed: 0"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should not create a subscription if plan id is not provided")
    public void newSubscriptionWithoutPlanId() throws Exception {
        NewSubscriptionPurchaseInput input = input();
        input.getPurchaseItems().get(0).setPlanId(null);

        mockMvc.perform(post(apiSuffix + "/purchase/subscription")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + authToken)
            .content(objectMapper.writeValueAsString(input))
        ).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should not create a subscription if purchase items has more than one item")
    public void newSubscriptionWithInvalidPurchaseItems() throws Exception {
        NewSubscriptionPurchaseInput input = input();
        input.setPurchaseItems(List.of(
            NewSubscriptionPurchaseInput.PurchaseItemInput.builder()
                .planId(planPurchased.getId())
                .build(),
            NewSubscriptionPurchaseInput.PurchaseItemInput.builder()
                .planId(planPurchased.getId())
                .build()
        ));

        mockMvc.perform(post(apiSuffix + "/purchase/subscription")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + authToken)
                .content(objectMapper.writeValueAsString(input))
            ).andExpect(jsonPath("$.message").value("Invalid purchase item. Can't insert a plan twice on purchase items!"))
            .andExpect(status().isBadRequest());
    }

    private NewSubscriptionPurchaseInput input() {
        return NewSubscriptionPurchaseInput.builder()
            .paymentMethod(EPaymentMethod.CREDIT_CARD)
            .installments(0)
            .purchaseItems(List.of(NewSubscriptionPurchaseInput.PurchaseItemInput.builder()
                .planId(planPurchased.getId())
                .build()
            )).build();
    }
}