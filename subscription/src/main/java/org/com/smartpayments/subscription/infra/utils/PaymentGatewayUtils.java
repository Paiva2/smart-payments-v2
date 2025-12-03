package org.com.smartpayments.subscription.infra.utils;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class PaymentGatewayUtils {
    @Value("${external.payment-gateway.uri}")
    private String apiUrl;

    @Value("${external.payment-gateway.api-key}")
    private String apiKey;

    private final String authHeader = "access_token";
}
