package org.com.smartpayments.authenticator.application.config.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.function.Supplier;

import static org.springframework.util.ObjectUtils.isEmpty;

@Component
@RequiredArgsConstructor
public class ServiceSignatureValidator implements AuthorizationManager<RequestAuthorizationContext> {
    private final InternalServiceConfig config;

    public final List<String> ALLOWED_SERVICES = List.of("scheduler");

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext context) {
        HttpServletRequest req = context.getRequest();

        String service = req.getHeader("X-Service-Name");
        String signature = req.getHeader("X-Service-Signature");
        String timestamp = req.getHeader("X-Timestamp");
        String userId = req.getHeader("X-User-Id");

        if (isEmpty(service) || isEmpty(signature) || isEmpty(timestamp) || isEmpty(userId) || isEmpty(config.getSecret())) {
            return new AuthorizationDecision(false);
        }

        if (!ALLOWED_SERVICES.contains(service)) {
            return new AuthorizationDecision(false);
        }

        String data = userId + ":" + timestamp;

        String expected = hmacSha256(data, config.getSecret());

        return new AuthorizationDecision(expected.equals(signature));
    }

    private String hmacSha256(String data, String secret) {
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
