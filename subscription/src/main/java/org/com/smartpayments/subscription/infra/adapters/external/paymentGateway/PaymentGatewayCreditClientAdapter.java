package org.com.smartpayments.subscription.infra.adapters.external.paymentGateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.smartpayments.subscription.core.common.exception.PaymentGatewayClientErrorException;
import org.com.smartpayments.subscription.core.domain.model.User;
import org.com.smartpayments.subscription.core.ports.out.external.dto.CreateCreditChargeOutput;
import org.com.smartpayments.subscription.core.ports.out.external.dto.DeleteChargeOutput;
import org.com.smartpayments.subscription.core.ports.out.external.dto.NewCreditChargeClientInput;
import org.com.smartpayments.subscription.core.ports.out.external.paymentGateway.PaymentGatewayCreditClientPort;
import org.com.smartpayments.subscription.infra.utils.PaymentGatewayUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@AllArgsConstructor
public class PaymentGatewayCreditClientAdapter implements PaymentGatewayCreditClientPort {
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private final static String CHARGES_PATH = "payments";

    private final PaymentGatewayUtils utils;

    private final RestTemplate restTemplate;
    private final RetryTemplate generalRetryTemplatePaymentGateway;

    @Override
    public CreateCreditChargeOutput createCreditCharge(User user, NewCreditChargeClientInput input) {
        String url = String.format("%s%s", utils.getApiUrl(), CHARGES_PATH);

        return generalRetryTemplatePaymentGateway.execute(ctx -> {
            log.info("[PaymentGatewayCreditClientAdapter#createCreditCharge]: user_id: {}, retry_attempt: {}", user.getId(), ctx.getRetryCount());

            try {
                String body = convertInputToString(user, input);
                HttpEntity<String> request = new HttpEntity<>(body, authHeader());

                String response = restTemplate.postForObject(url, request, String.class);

                return ((CreateCreditChargeOutput) convertResponseOutput(response, CreateCreditChargeOutput.class));
            } catch (HttpStatusCodeException e) {
                log.error("[PaymentGatewayCreditClientAdapter#createCreditCharge]: user_id: {}, status: {}, body: {}",
                    user.getId(),
                    e.getStatusCode(),
                    e.getResponseBodyAsString()
                );

                if (e.getStatusCode().is4xxClientError()) {
                    if (e.getStatusCode().equals(HttpStatus.TOO_MANY_REQUESTS)) {
                        throw e;
                    }

                    throw new PaymentGatewayClientErrorException("Client error while creating charge on payment gateway!");
                }

                throw e;
            }
        });
    }

    @Override
    public DeleteChargeOutput deleteCharge(User user, String chargeId) {
        String url = String.format("%s%s/%s", utils.getApiUrl(), CHARGES_PATH, chargeId);

        return generalRetryTemplatePaymentGateway.execute(ctx -> {
            log.info("[PaymentGatewayCreditClientAdapter#deleteCharge]: user_id: {}, retry_attempt: {}", user.getId(), ctx.getRetryCount());

            try {
                HttpEntity<String> request = new HttpEntity<>(authHeader());

                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);

                return ((DeleteChargeOutput) convertResponseOutput(response.getBody(), DeleteChargeOutput.class));
            } catch (HttpStatusCodeException e) {
                log.error("[PaymentGatewayCreditClientAdapter#deleteCharge]: user_id: {}, status: {}, body: {}",
                    user.getId(),
                    e.getStatusCode(),
                    e.getResponseBodyAsString()
                );

                if (e.getStatusCode().is4xxClientError()) {
                    if (e.getStatusCode().equals(HttpStatus.TOO_MANY_REQUESTS)) {
                        throw e;
                    }

                    throw new PaymentGatewayClientErrorException("Client error while deleting charge on payment gateway!");
                }

                throw e;
            }
        });
    }

    private String convertInputToString(User user, Object input) {
        try {
            return objectMapper.writeValueAsString(input);
        } catch (Exception e) {
            log.error("[PaymentGatewayCreditClientAdapter#convertInputToString]: user_id: {}, message: {}",
                user.getId(),
                e.getMessage()
            );
            throw new PaymentGatewayClientErrorException("Client error while creating charge on payment gateway!");
        }
    }

    private Object convertResponseOutput(String body, Class<?> responseClass) {
        try {
            return objectMapper.readValue(body, responseClass);
        } catch (Exception e) {
            log.error("[PaymentGatewayCreditClientAdapter#convertResponseOutput]: message: {}", e.getMessage());
            throw new PaymentGatewayClientErrorException("Client error while creating charge on payment gateway!");
        }
    }

    private HttpHeaders authHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(utils.getAuthHeader(), utils.getApiKey());
        return headers;
    }
}
