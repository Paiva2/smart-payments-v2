package org.com.smartpayments.subscription.integration.fixtures.wiremock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.com.smartpayments.subscription.core.domain.enums.EPurchaseChargeStatus;
import org.com.smartpayments.subscription.core.ports.out.external.dto.CreateCreditChargeOutput;
import org.com.smartpayments.subscription.core.ports.out.external.dto.CreateSubscriptionOutput;
import org.com.smartpayments.subscription.core.ports.out.external.dto.DeleteSubscriptionOutput;
import org.com.smartpayments.subscription.core.ports.out.external.dto.GetSubscriptionChargesOutput;
import org.com.smartpayments.subscription.core.ports.out.external.dto.NewClientPaymentGatewayOutput;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class PaymentGatewayStubs {
    private final static String createUserPath = "/v3/customers";
    private final static String deleteUserPath = "/v3/customers/.*";
    private final static String createSubscriptionPath = "/v3/subscriptions";
    private final static String createCreditChargePath = "/v3/payments";
    private final static String deleteSubscriptionPath = "/v3/subscriptions/.*";
    private final static String listSubscriptionChargesPath = "/v3/subscriptions/.*/payments";

    public static void mockCreateSubscription(WireMockServer server, ObjectMapper objectMapper, String id) throws Exception {
        server.stubFor(WireMock.post(urlPathEqualTo(createSubscriptionPath))
            .willReturn(okJson(objectMapper.writeValueAsString(
                CreateSubscriptionOutput.builder()
                    .id(id)
                    .build()
            ))));
    }

    public static void mockCreateCreditCharge(WireMockServer server, ObjectMapper objectMapper, String id) throws Exception {
        server.stubFor(WireMock.post(urlPathEqualTo(createCreditChargePath))
            .willReturn(okJson(objectMapper.writeValueAsString(
                CreateCreditChargeOutput.builder()
                    .id(id)
                    .subscription(null)
                    .paymentLink("any_link")
                    .invoiceUrl("any_url")
                    .build()
            ))));
    }

    public static void mockDeleteSubscription(WireMockServer server, ObjectMapper objectMapper) throws Exception {
        server.stubFor(WireMock.delete(urlPathMatching(deleteSubscriptionPath))
            .willReturn(okJson(objectMapper.writeValueAsString(
                DeleteSubscriptionOutput.builder()
                    .id("any_subscription_id" + UUID.randomUUID())
                    .deleted(true)
                    .build()
            ))));
    }

    public static void mockCreateUser(WireMockServer server, ObjectMapper objectMapper) throws Exception {
        server.stubFor(WireMock.post(urlPathEqualTo(createUserPath))
            .willReturn(
                okJson(objectMapper.writeValueAsString(new NewClientPaymentGatewayOutput("payment_gateway_external_id" + UUID.randomUUID()))
                )
            )
        );
    }

    public static void mockDeleteUser(WireMockServer server) {
        server.stubFor(WireMock.delete(urlPathMatching(deleteUserPath))
            .willReturn(ok())
        );
    }

    public static void mockListSubscriptionCharges(WireMockServer server, ObjectMapper objectMapper) throws Exception {
        server.stubFor(WireMock.get(urlPathMatching(listSubscriptionChargesPath))
            .willReturn(okJson(objectMapper.writeValueAsString(
                GetSubscriptionChargesOutput.builder()
                    .data(List.of(
                        GetSubscriptionChargesOutput.DataOutput.builder()
                            .id(UUID.randomUUID().toString())
                            .dateCreated(new Date())
                            .subscription("any_id")
                            .invoiceUrl("any_url")
                            .description("any_description")
                            .paymentDate(null)
                            .dueDate(new Date())
                            .bankSlipUrl("any_url")
                            .value(new BigDecimal("5"))
                            .billingType(null)
                            .pixTransaction(null)
                            .pixQrCodeId(null)
                            .status(EPurchaseChargeStatus.PENDING.toString())
                            .externalReference(null)
                            .build()
                    )).build()
            ))));
    }
}
