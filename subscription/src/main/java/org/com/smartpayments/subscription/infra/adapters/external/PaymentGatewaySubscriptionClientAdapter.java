package org.com.smartpayments.subscription.infra.adapters.external;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.smartpayments.subscription.core.domain.common.exception.PaymentGatewayClientErrorException;
import org.com.smartpayments.subscription.core.domain.core.ports.out.external.PaymentGatewaySubscriptionClientPort;
import org.com.smartpayments.subscription.core.domain.core.ports.out.external.dto.CreateSubscriptionOutput;
import org.com.smartpayments.subscription.core.domain.core.ports.out.external.dto.DeleteSubscriptionOutput;
import org.com.smartpayments.subscription.core.domain.core.ports.out.external.dto.GetSubscriptionChargesOutput;
import org.com.smartpayments.subscription.core.domain.core.ports.out.external.dto.NewSubscriptionClientInput;
import org.com.smartpayments.subscription.core.domain.model.User;
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
public class PaymentGatewaySubscriptionClientAdapter implements PaymentGatewaySubscriptionClientPort {
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private final static String SUBSCRIPTION_PATH = "subscriptions";

    private final PaymentGatewayUtils utils;

    private final RestTemplate restTemplate;
    private final RetryTemplate generalRetryTemplatePaymentGateway;

    @Override
    public CreateSubscriptionOutput createSubscription(User user, NewSubscriptionClientInput input) {
        String url = String.format("%s%s", utils.getApiUrl(), SUBSCRIPTION_PATH);

        return generalRetryTemplatePaymentGateway.execute(ctx -> {
            log.info("[PaymentGatewaySubscriptionClientAdapter#createSubscription]: user_id: {}, retry_attempt: {}", user.getId(), ctx.getRetryCount());

            try {
                String body = convertInputToString(user, input);
                HttpEntity<String> request = new HttpEntity<>(body, authHeader());

                String response = restTemplate.postForObject(url, request, String.class);

                return ((CreateSubscriptionOutput) convertResponseOutput(response, CreateSubscriptionOutput.class));
            } catch (HttpStatusCodeException e) {
                log.error("[PaymentGatewaySubscriptionClientAdapter#createSubscription]: user_id: {}, status: {}, body: {}",
                    user.getId(),
                    e.getStatusCode(),
                    e.getResponseBodyAsString()
                );

                if (e.getStatusCode().is4xxClientError()) {
                    if (e.getStatusCode().equals(HttpStatus.TOO_MANY_REQUESTS)) {
                        throw e;
                    }

                    throw new PaymentGatewayClientErrorException("Client error while creating subscription on payment gateway!");
                }

                throw e;
            }
        });
    }

    @Override
    public GetSubscriptionChargesOutput getSubscriptionCharges(User user, String subscriptionId) {
        String url = String.format("%s%s/%s/payments", utils.getApiUrl(), SUBSCRIPTION_PATH, subscriptionId);

        return generalRetryTemplatePaymentGateway.execute(ctx -> {
            log.info("[PaymentGatewaySubscriptionClientAdapter#getSubscriptionCharges]: user_id: {}, retry_attempt: {}", user.getId(), ctx.getRetryCount());

            try {
                HttpEntity<String> request = new HttpEntity<>(authHeader());

                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

                return ((GetSubscriptionChargesOutput) convertResponseOutput(response.getBody(), GetSubscriptionChargesOutput.class));
            } catch (HttpStatusCodeException e) {
                log.error("[PaymentGatewaySubscriptionClientAdapter#getSubscriptionCharges]: user_id: {}, status: {}, body: {}",
                    user.getId(),
                    e.getStatusCode(),
                    e.getResponseBodyAsString()
                );

                if (e.getStatusCode().is4xxClientError()) {
                    if (e.getStatusCode().equals(HttpStatus.TOO_MANY_REQUESTS)) {
                        throw e;
                    }

                    throw new PaymentGatewayClientErrorException("Client error while fetching subscription charges on payment gateway!");
                }

                throw e;
            }
        });
    }

    @Override
    public DeleteSubscriptionOutput deleteSubscription(User user, String subscriptionId) {
        String url = String.format("%s%s/%s", utils.getApiUrl(), SUBSCRIPTION_PATH, subscriptionId);

        return generalRetryTemplatePaymentGateway.execute(ctx -> {
            log.info("[PaymentGatewaySubscriptionClientAdapter#deleteSubscription]: user_id: {}, retry_attempt: {}", user.getId(), ctx.getRetryCount());

            try {
                HttpEntity<String> request = new HttpEntity<>(authHeader());

                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

                return ((DeleteSubscriptionOutput) convertResponseOutput(response.getBody(), DeleteSubscriptionOutput.class));
            } catch (HttpStatusCodeException e) {
                log.error("[PaymentGatewaySubscriptionClientAdapter#deleteSubscription]: user_id: {}, status: {}, body: {}",
                    user.getId(),
                    e.getStatusCode(),
                    e.getResponseBodyAsString()
                );

                if (e.getStatusCode().is4xxClientError()) {
                    if (e.getStatusCode().equals(HttpStatus.TOO_MANY_REQUESTS)) {
                        throw e;
                    }

                    throw new PaymentGatewayClientErrorException("Client error while deleting subscription on payment gateway!");
                }

                throw e;
            }
        });
    }

    private String convertInputToString(User user, Object input) {
        try {
            return objectMapper.writeValueAsString(input);
        } catch (Exception e) {
            log.error("[PaymentGatewaySubscriptionClientAdapter#convertInputToString]: user_id: {}, message: {}",
                user.getId(),
                e.getMessage()
            );
            throw new PaymentGatewayClientErrorException("Client error while creating subscription on payment gateway!");
        }
    }

    private Object convertResponseOutput(String body, Class<?> responseClass) {
        try {
            return objectMapper.readValue(body, responseClass);
        } catch (Exception e) {
            log.error("[PaymentGatewaySubscriptionClientAdapter#convertResponseOutput]: message: {}", e.getMessage());
            throw new PaymentGatewayClientErrorException("Client error while creating subscription on payment gateway!");
        }
    }

    private HttpHeaders authHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(utils.getAuthHeader(), utils.getApiKey());
        return headers;
    }
}
