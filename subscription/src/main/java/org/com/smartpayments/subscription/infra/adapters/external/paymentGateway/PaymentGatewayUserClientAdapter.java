package org.com.smartpayments.subscription.infra.adapters.external.paymentGateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.smartpayments.subscription.core.domain.common.exception.PaymentGatewayClientErrorException;
import org.com.smartpayments.subscription.core.domain.core.ports.out.external.dto.NewClientPaymentGatewayClientInput;
import org.com.smartpayments.subscription.core.domain.core.ports.out.external.dto.NewClientPaymentGatewayOutput;
import org.com.smartpayments.subscription.core.domain.core.ports.out.external.paymentGateway.PaymentGatewayUserClientPort;
import org.com.smartpayments.subscription.core.domain.model.User;
import org.com.smartpayments.subscription.infra.utils.PaymentGatewayUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@AllArgsConstructor
public class PaymentGatewayUserClientAdapter implements PaymentGatewayUserClientPort {
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private final static String CLIENT_PATH = "customers";

    private final PaymentGatewayUtils utils;

    private final RestTemplate restTemplate;
    private final RetryTemplate generalRetryTemplatePaymentGateway;

    @Override
    public String newUserClient(User user) {
        String url = String.format("%s%s", utils.getApiUrl(), CLIENT_PATH);

        return generalRetryTemplatePaymentGateway.execute(ctx -> {
            log.info("[PaymentGatewayUserClientAdapter#newUserClient]: user_id: {}, retry_attempt: {}", user.getId(), ctx.getRetryCount());

            try {
                String body = newUserClientMountInput(user);
                HttpEntity<String> request = new HttpEntity<>(body, authHeader());

                String response = restTemplate.postForObject(url, request, String.class);

                return ((NewClientPaymentGatewayOutput) convertResponseOutput(response, NewClientPaymentGatewayOutput.class)).getId();
            } catch (HttpStatusCodeException e) {
                log.error("[PaymentGatewayUserClientAdapter#newUserClient]: user_id: {}, status: {}, body: {}",
                    user.getId(),
                    e.getStatusCode(),
                    e.getResponseBodyAsString()
                );

                if (e.getStatusCode().is4xxClientError()) {
                    if (e.getStatusCode().equals(HttpStatus.TOO_MANY_REQUESTS)) {
                        throw e;
                    }

                    throw new PaymentGatewayClientErrorException("Client error while creating user on payment gateway!");
                }

                throw e;
            }
        });
    }

    @Override
    public void deleteUserClient(User user) {
        String url = String.format("%s%s/%s", utils.getApiUrl(), CLIENT_PATH, user.getUserPaymentGatewayExternalId());

        generalRetryTemplatePaymentGateway.execute(ctx -> {
            log.info("[PaymentGatewayUserClientAdapter#deleteUserClient]: user_id: {}, retry_attempt: {}", user.getId(), ctx.getRetryCount());

            try {
                HttpEntity<String> request = new HttpEntity<>(authHeader());
                restTemplate.exchange(url, HttpMethod.DELETE, request, Void.class);
                return null;
            } catch (HttpStatusCodeException e) {
                log.error("[PaymentGatewayUserClientAdapter#deleteUserClient]: user_id: {}, status: {}, body: {}",
                    user.getId(),
                    e.getStatusCode(),
                    e.getResponseBodyAsString()
                );

                if (e.getStatusCode().is4xxClientError()) {
                    if (e.getStatusCode().equals(HttpStatus.TOO_MANY_REQUESTS)) {
                        throw e;
                    }

                    throw new PaymentGatewayClientErrorException("Client error while deleting user on payment gateway!");
                }

                throw e;
            }
        });
    }

    private String newUserClientMountInput(User user) {
        try {
            NewClientPaymentGatewayClientInput input = NewClientPaymentGatewayClientInput.builder()
                .name(user.getFirstName().concat(" ").concat(user.getLastName()))
                .cpfCnpj(user.getCpfCnpj())
                .email(user.getEmail())
                .phone(user.getPhone())
                .mobilePhone(user.getPhone())
                .address(user.getAddress().getStreet())
                .addressNumber(user.getAddress().getNumber())
                .complement(user.getAddress().getComplement())
                .province(user.getAddress().getNeighborhood())
                .postalCode(user.getAddress().getZipcode())
                .externalReference(user.getId().toString())
                .build();

            return objectMapper.writeValueAsString(input);
        } catch (Exception e) {
            log.error("[PaymentGatewayUserClientAdapter#newUserClientMountInput]: user_id: {}, message: {}",
                user.getId(),
                e.getMessage()
            );
            throw new PaymentGatewayClientErrorException("Client error while creating user on payment gateway!");
        }
    }

    private Object convertResponseOutput(String body, Class<?> responseClass) {
        try {
            return objectMapper.readValue(body, responseClass);
        } catch (Exception e) {
            log.error("[PaymentGatewayUserClientAdapter#convertResponseOutput]: message: {}", e.getMessage());
            throw new PaymentGatewayClientErrorException("Client error while creating user on payment gateway!");
        }
    }

    private HttpHeaders authHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(utils.getAuthHeader(), utils.getApiKey());
        return headers;
    }
}
