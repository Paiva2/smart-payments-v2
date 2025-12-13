package org.com.smartpayments.subscription.integration.core.domain.webhook.purchaseCharge;

import org.com.smartpayments.subscription.core.domain.enums.ECredit;
import org.com.smartpayments.subscription.core.domain.enums.EPaymentGatewayEvent;
import org.com.smartpayments.subscription.core.domain.enums.EPaymentMethod;
import org.com.smartpayments.subscription.core.domain.enums.EPurchaseChargeStatus;
import org.com.smartpayments.subscription.core.domain.enums.EPurchaseType;
import org.com.smartpayments.subscription.core.domain.model.Credit;
import org.com.smartpayments.subscription.core.domain.model.Purchase;
import org.com.smartpayments.subscription.core.domain.model.User;
import org.com.smartpayments.subscription.core.ports.in.dto.PaymentGatewayWebhookInput;
import org.com.smartpayments.subscription.infra.persistence.repository.CreditRepository;
import org.com.smartpayments.subscription.integration.fixtures.bases.IntegrationTestBase;
import org.com.smartpayments.subscription.integration.utils.PurchaseUtils;
import org.com.smartpayments.subscription.integration.utils.UserUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Date;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CreatePurchaseChargeTest extends IntegrationTestBase {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserUtils userUtils;

    @Autowired
    private PurchaseUtils purchaseUtils;

    @Autowired
    private CreditRepository creditRepository;

    @Value("${server.api-suffix}")
    private String apiSuffix;

    @Value("${external.payment-gateway.webhook.api-key}")
    private String webhookToken;

    @Value("${spring.kafka.topics.new-purchase-charge}")
    private String purchaseChargeCreatedTopic;

    private User user;

    private Credit creditPurchased;

    @MockitoSpyBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @BeforeEach
    public void setup() {
        this.creditPurchased = creditRepository.findByType(ECredit.EMAIL).get();

        this.user = userUtils.createUser(true, true);
    }

    @Test
    @DisplayName("Should create a purchase charged sent from webhook")
    public void newCreditPurchaseCharge() throws Exception {
        Purchase purchase = purchaseUtils.createPurchase(EPurchaseType.CREDIT, this.user, null, this.creditPurchased);

        PaymentGatewayWebhookInput input = input(purchase.getExternalId());

        mockMvc.perform(post(apiSuffix + "/purchase/webhook")
            .contentType(MediaType.APPLICATION_JSON)
            .header("asaas-access-token", webhookToken)
            .content(objectMapper.writeValueAsString(input))
        ).andExpect(status().isNoContent());

        verify(kafkaTemplate, times(1)).send(
            eq(purchaseChargeCreatedTopic),
            eq(input.getPayment().getId()),
            anyString()
        );
    }

    private PaymentGatewayWebhookInput input(String purchaseExtId) {
        return PaymentGatewayWebhookInput.builder()
            .id("event_id")
            .event(EPaymentGatewayEvent.PAYMENT_CREATED.name())
            .dateCreated(new Date().toString())
            .payment(PaymentGatewayWebhookInput.PaymentGatewayPaymentInput.builder()
                .id(purchaseExtId)
                .customer(user.getUserPaymentGatewayExternalId())
                .subscription(null)
                .value(BigDecimal.valueOf(20))
                .description("My Purchase")
                .billingType(EPaymentMethod.CREDIT_CARD.name())
                .confirmedDate(null)
                .status(EPurchaseChargeStatus.PENDING.name())
                .dueDate(new Date())
                .paymentDate(null)
                .pixTransaction(null)
                .pixQrCode(null)
                .invoiceUrl("any_url")
                .externalReference(null)
                .transactionReceiptUrl("any_url")
                .bankSlipUrl("any_url")
                .build()
            ).build();
    }
}
