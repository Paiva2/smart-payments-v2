package com.smartpayments.scheduler.infra.adapters.external.subscription;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartpayments.scheduler.core.common.exception.GenericClientException;
import com.smartpayments.scheduler.core.common.exception.SubscriptionClientErrorException;
import com.smartpayments.scheduler.core.ports.in.external.messaging.UserSubscriptionCreditInput;
import com.smartpayments.scheduler.core.ports.out.external.dto.ConsumeUserSubscriptionCreditsOutput;
import com.smartpayments.scheduler.core.ports.out.external.dto.UserSubscriptionOutput;
import com.smartpayments.scheduler.core.ports.out.external.subscription.SubscriptionClientPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionClientAdapter implements SubscriptionClientPort {
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private final static String USER_SUBSCRIPTION_PATH = "user-subscription";

    @Value("${external.subscription.uri}")
    private String subscriptionUri;

    @Value("${external.authenticator.external-services-key}")
    private String externalServicesSecret;

    @Value("${spring.application.name}")
    private String SERVICE_NAME;

    private final RestTemplate restTemplate;

    private final RetryTemplate generalRetryTemplateSubscriptionService;

    @Override
    public UserSubscriptionOutput getUserSubscription(Long userId) {
        try {
            String url = String.format("%s%s/internal", subscriptionUri, USER_SUBSCRIPTION_PATH);

            HttpEntity<String> request = new HttpEntity<>(mountHeaders(userId));
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

            return (UserSubscriptionOutput) convertResponseOutput(response.getBody(), UserSubscriptionOutput.class);
        } catch (HttpStatusCodeException e) {
            log.error("[SubscriptionClientAdapter#getUserSubscription]: user_id: {}, status: {}, body: {}",
                userId,
                e.getStatusCode(),
                e.getResponseBodyAsString()
            );

            throw new SubscriptionClientErrorException("Client error while fetching user on subscription!");
        }
    }

    @Override
    public ConsumeUserSubscriptionCreditsOutput consumeUserSubscriptionCredits(String idempotencyHash, UserSubscriptionCreditInput input) {
        String url = String.format("%s%s/credits/consume/internal", subscriptionUri, USER_SUBSCRIPTION_PATH);

        return generalRetryTemplateSubscriptionService.execute(ctx -> {
            try {
                log.info("[SubscriptionClientAdapter#updateUserSubscriptionCredits]: user_id: {}, retry_attempt: {}",
                    input.getUserId(),
                    ctx.getRetryCount()
                );

                HttpHeaders headers = mountHeaders(input.getUserId());
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setAccept(List.of(MediaType.APPLICATION_JSON));
                headers.add("X-IDEMPOTENCY-KEY", idempotencyHash);

                HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(input), headers);
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, request, String.class);
                return (ConsumeUserSubscriptionCreditsOutput) convertResponseOutput(response.getBody(), ConsumeUserSubscriptionCreditsOutput.class);
            } catch (HttpStatusCodeException e) {
                log.error("[SubscriptionClientAdapter#updateUserSubscriptionCredits]: user_id: {}, status: {}, body: {}",
                    input.getUserId(),
                    e.getStatusCode(),
                    e.getResponseBodyAsString()
                );

                if (e.getStatusCode().is4xxClientError()) {
                    throw new SubscriptionClientErrorException("Client error while updating user credits on subscription!");
                }

                throw e;
            } catch (Exception e) {
                log.error("[SubscriptionClientAdapter#updateUserSubscriptionCredits]: user_id: {}, exception: {}",
                    input.getUserId(),
                    e.getMessage(),
                    e
                );

                throw new GenericClientException(e.getMessage());
            }
        });
    }

    private Object convertResponseOutput(String body, Class<?> responseClass) {
        try {
            return objectMapper.readValue(body, responseClass);
        } catch (Exception e) {
            log.error("[SubscriptionClientAdapter#convertResponseOutput]: message: {}", e.getMessage());
            throw new SubscriptionClientErrorException("Client error while fetching user on subscription!");
        }
    }

    private HttpHeaders mountHeaders(Long userId) {
        HttpHeaders headers = new HttpHeaders();

        String timestamp = String.valueOf(new Date().getTime());

        String dataExpected = String.format("%d:%s", userId, timestamp);

        headers.add("X-Service-Name", SERVICE_NAME);
        headers.add("X-Service-Signature", handleSignature(dataExpected, externalServicesSecret));
        headers.add("X-Timestamp", timestamp);
        headers.add("X-User-Id", userId.toString());
        return headers;
    }

    private String handleSignature(String data, String secret) {
        try {
            String algorithm = "HmacSHA256";
            Mac sha256_HMAC = Mac.getInstance(algorithm);
            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), algorithm);
            sha256_HMAC.init(keySpec);

            byte[] hash = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            throw new RuntimeException("Error while generating HMAC hash", e);
        }
    }
}
