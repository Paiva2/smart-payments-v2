package org.com.smartpayments.subscription.integration.core.domain.usecase.purchase.newCreditsPurchase;

import org.com.smartpayments.subscription.core.domain.enums.ECredit;
import org.com.smartpayments.subscription.core.domain.enums.EPaymentMethod;
import org.com.smartpayments.subscription.core.domain.enums.EPurchaseStatus;
import org.com.smartpayments.subscription.core.domain.enums.EPurchaseType;
import org.com.smartpayments.subscription.core.domain.model.Credit;
import org.com.smartpayments.subscription.core.domain.model.Purchase;
import org.com.smartpayments.subscription.core.domain.model.PurchaseItem;
import org.com.smartpayments.subscription.core.domain.model.User;
import org.com.smartpayments.subscription.core.ports.in.dto.NewCreditsPurchaseInput;
import org.com.smartpayments.subscription.core.ports.out.external.paymentGateway.PaymentGatewayUserClientPort;
import org.com.smartpayments.subscription.infra.persistence.repository.CreditRepository;
import org.com.smartpayments.subscription.infra.persistence.repository.PurchaseItemRepository;
import org.com.smartpayments.subscription.infra.persistence.repository.PurchaseRepository;
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

public class NewCreditsPurchaseTest extends IntegrationTestBase {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserUtils userUtils;

    @Autowired
    private CreditRepository creditRepository;

    @MockitoSpyBean
    private PaymentGatewayUserClientPort paymentGatewayUserClientPort;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private PurchaseItemRepository purchaseItemRepository;

    @Value("${server.api-suffix}")
    private String apiSuffix;

    private String authToken;

    private Credit creditPurchased;

    @BeforeEach
    public void setUp() throws Exception {
        creditPurchased = creditRepository.findByType(ECredit.EMAIL).get();

        User user = userUtils.createUser(true, true);
        authToken = userUtils.generateAuthToken(user.getId());

        AuthenticatorStubs.mockAuthenticateUser(wireMockServer, objectMapper, user);

        PaymentGatewayStubs.mockCreateUser(wireMockServer, objectMapper);
    }

    @Test
    @DisplayName("Should create a credit purchase with user already having payment gateway register")
    public void newCreditPurchase() throws Exception {
        String creditId = "any_credit_charge_id" + UUID.randomUUID();
        PaymentGatewayStubs.mockCreateCreditCharge(wireMockServer, objectMapper, creditId);

        NewCreditsPurchaseInput input = input();

        mockMvc.perform(post(apiSuffix + "/purchase/credit")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + authToken)
                .content(objectMapper.writeValueAsString(input))
            ).andExpect(jsonPath("$.paymentUrl").isNotEmpty())
            .andExpect(status().isOk());

        verify(paymentGatewayUserClientPort, times(0)).newUserClient(any());

        Optional<Purchase> purchase = purchaseRepository.findByExternalId(creditId);

        assertTrue(purchase.isPresent());
        assertEquals(creditId, purchase.get().getExternalId());
        assertEquals(EPurchaseStatus.PENDING, purchase.get().getStatus());
        assertEquals(EPurchaseType.CREDIT, purchase.get().getPurchaseType());
        assertEquals(0, purchase.get().getInstallments());

        List<PurchaseItem> purchaseItems = purchaseItemRepository.findByPurchaseId(purchase.get().getId());

        assertFalse(purchaseItems.isEmpty());
        assertEquals(purchaseItems.get(0).getPurchase().getId(), purchase.get().getId());
        assertEquals(purchaseItems.get(0).getValue(), creditPurchased.getValue());
        assertNull(purchaseItems.get(0).getPlan());
    }

    @Test
    @DisplayName("Should create a credit purchase with user not having payment gateway register")
    public void newCreditPurchaseWithUserNotRegisteredOnGateway() throws Exception {
        String creditId = "any_credit_charge_id" + UUID.randomUUID();
        PaymentGatewayStubs.mockCreateCreditCharge(wireMockServer, objectMapper, creditId);

        User otherUser = userUtils.createUser(300L, RANDOM_VALID_CPF2, true, false);
        String authToken = userUtils.generateAuthToken(otherUser.getId());

        NewCreditsPurchaseInput input = input();

        mockMvc.perform(post(apiSuffix + "/purchase/credit")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + authToken)
                .content(objectMapper.writeValueAsString(input))
            ).andExpect(jsonPath("$.paymentUrl").isNotEmpty())
            .andExpect(status().isOk());

        verify(paymentGatewayUserClientPort, times(1)).newUserClient(any());

        Optional<Purchase> purchase = purchaseRepository.findByExternalId(creditId);

        assertTrue(purchase.isPresent());
        assertEquals(creditId, purchase.get().getExternalId());
        assertEquals(EPurchaseStatus.PENDING, purchase.get().getStatus());
        assertEquals(EPurchaseType.CREDIT, purchase.get().getPurchaseType());
        assertEquals(0, purchase.get().getInstallments());

        List<PurchaseItem> purchaseItems = purchaseItemRepository.findByPurchaseId(purchase.get().getId());

        assertFalse(purchaseItems.isEmpty());
        assertEquals(purchaseItems.get(0).getPurchase().getId(), purchase.get().getId());
        assertEquals(purchaseItems.get(0).getValue(), creditPurchased.getValue());
        assertNull(purchaseItems.get(0).getPlan());
    }

    @Test
    @DisplayName("Should not create purchase if purchase items list is empty")
    public void newCreditPurchaseWithoutPurchaseItems() throws Exception {
        String creditId = "any_credit_charge_id" + UUID.randomUUID();
        PaymentGatewayStubs.mockCreateCreditCharge(wireMockServer, objectMapper, creditId);

        NewCreditsPurchaseInput input = input();
        input.setPurchaseItems(null);

        mockMvc.perform(post(apiSuffix + "/purchase/credit")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + authToken)
            .content(objectMapper.writeValueAsString(input))
        ).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should not create purchase if credit id is null")
    public void newCreditPurchaseWithoutCreditId() throws Exception {
        String creditId = "any_credit_charge_id" + UUID.randomUUID();
        PaymentGatewayStubs.mockCreateCreditCharge(wireMockServer, objectMapper, creditId);

        NewCreditsPurchaseInput input = input();
        input.getPurchaseItems().get(0).setCreditId(null);

        mockMvc.perform(post(apiSuffix + "/purchase/credit")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + authToken)
            .content(objectMapper.writeValueAsString(input))
        ).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should not create purchase if quantity is invalid")
    public void newCreditPurchaseWithoutValidQuantity() throws Exception {
        String creditId = "any_credit_charge_id" + UUID.randomUUID();
        PaymentGatewayStubs.mockCreateCreditCharge(wireMockServer, objectMapper, creditId);

        NewCreditsPurchaseInput input = input();
        input.getPurchaseItems().get(0).setQuantity(-1);

        mockMvc.perform(post(apiSuffix + "/purchase/credit")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + authToken)
            .content(objectMapper.writeValueAsString(input))
        ).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should not create purchase if credit id is repeated")
    public void newCreditPurchaseWithRepeatCreditId() throws Exception {
        String creditId = "any_credit_charge_id" + UUID.randomUUID();
        PaymentGatewayStubs.mockCreateCreditCharge(wireMockServer, objectMapper, creditId);

        NewCreditsPurchaseInput input = input();
        input.setPurchaseItems(List.of(
            NewCreditsPurchaseInput.PurchaseItemInput.builder()
                .creditId(creditPurchased.getId()) // repeat id
                .quantity(1)
                .build(),
            NewCreditsPurchaseInput.PurchaseItemInput.builder()
                .creditId(creditPurchased.getId()) // repeat id
                .quantity(1)
                .build()
        ));

        mockMvc.perform(post(apiSuffix + "/purchase/credit")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + authToken)
                .content(objectMapper.writeValueAsString(input))
            ).andExpect(jsonPath("$.message").value("Invalid purchase item. Can't repeat a credit id on purchase items, instead increase quantity or remove duplicated!"))
            .andExpect(status().isBadRequest());
    }

    private NewCreditsPurchaseInput input() {
        return NewCreditsPurchaseInput.builder()
            .paymentMethod(EPaymentMethod.CREDIT_CARD)
            .purchaseItems(List.of(NewCreditsPurchaseInput.PurchaseItemInput.builder()
                .creditId(creditPurchased.getId())
                .quantity(1)
                .build()
            )).build();
    }
}
