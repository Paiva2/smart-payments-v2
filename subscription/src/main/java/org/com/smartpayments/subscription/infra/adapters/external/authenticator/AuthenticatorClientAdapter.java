package org.com.smartpayments.subscription.infra.adapters.external.authenticator;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.smartpayments.subscription.core.common.exception.AuthenticatorClientErrorException;
import org.com.smartpayments.subscription.core.ports.out.external.authenticator.AuthenticatorClientPort;
import org.com.smartpayments.subscription.core.ports.out.external.dto.UserAuthenticatorOutput;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticatorClientAdapter implements AuthenticatorClientPort {
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private final static String USER_AUTH_PATH = "user/internal";

    @Value("${external.authenticator.uri}")
    private String authenticatorUri;

    @Value("${external.authenticator.external-services-key}")
    private String externalServicesSecret;

    @Value("${spring.application.name}")
    private String SERVICE_NAME;

    private final RestTemplate restTemplate;

    @Override
    public UserAuthenticatorOutput findUser(Long userId) {
        try {
            String url = String.format("%s%s", authenticatorUri, USER_AUTH_PATH);

            HttpEntity<String> request = new HttpEntity<>(mountHeaders(userId));
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

            return (UserAuthenticatorOutput) convertResponseOutput(response.getBody(), UserAuthenticatorOutput.class);
        } catch (HttpStatusCodeException e) {
            log.error("[AuthenticatorClientAdapter#findUser]: user_id: {}, status: {}, body: {}",
                userId,
                e.getStatusCode(),
                e.getResponseBodyAsString()
            );

            throw new AuthenticatorClientErrorException("Client error while fetching user on authenticator!");
        }
    }

    private Object convertResponseOutput(String body, Class<?> responseClass) {
        try {
            return objectMapper.readValue(body, responseClass);
        } catch (Exception e) {
            log.error("[AuthenticatorClientAdapter#convertResponseOutput]: message: {}", e.getMessage());
            throw new AuthenticatorClientErrorException("Client error while fetching user on authenticator!");
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
