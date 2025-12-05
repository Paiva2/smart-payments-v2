package org.com.smartpayments.subscription.integration.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;

@Component
public class JwtUtilsTest {
    private final static String ISSUER = "smart_payments";

    @Value("${auth.jwt.secret}")
    private String SECRET;

    public String generateAuthJwt(Long subjectId, int expirationInDays) {
        Date expiresAt = getExpiresAt(expirationInDays);

        return JWT.create()
            .withIssuer(ISSUER)
            .withExpiresAt(expiresAt)
            .withSubject(subjectId.toString())
            .withClaim("expires_at", expiresAt.toString())
            .sign(getAlgorithm());
    }

    private Algorithm getAlgorithm() {
        return Algorithm.HMAC256(SECRET);
    }

    private Date getExpiresAt(int expDays) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, expDays);
        return calendar.getTime();
    }
}
