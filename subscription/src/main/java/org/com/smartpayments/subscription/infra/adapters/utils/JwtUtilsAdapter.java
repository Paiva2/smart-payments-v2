package org.com.smartpayments.subscription.infra.adapters.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import org.com.smartpayments.subscription.core.domain.common.exception.InvalidAuthTokenException;
import org.com.smartpayments.subscription.core.domain.core.ports.out.utils.JwtUtilsPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtUtilsAdapter implements JwtUtilsPort {
    private final static String ISSUER = "smart_payments";

    @Value("${auth.jwt.secret}")
    private String SECRET;

    @Override
    public DecodedJWT verifyAuthJwt(String authToken) {
        try {
            JWTVerifier verifier = JWT.require(getAlgorithm())
                .withIssuer(ISSUER)
                .withClaimPresence("expires_at")
                .build();

            return verifier.verify(authToken);
        } catch (JWTVerificationException e) {
            log.error("JWT verification failed", e);
            throw new InvalidAuthTokenException();
        }
    }

    private Algorithm getAlgorithm() {
        return Algorithm.HMAC256(SECRET);
    }
}
