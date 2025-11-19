package org.com.smartpayments.authenticator.core.ports.out.utils;

import com.auth0.jwt.interfaces.DecodedJWT;

public interface JwtUtilsPort {
    String generateAuthJwt(Long subjectId, int expirationInDays);

    DecodedJWT verifyAuthJwt(String authToken);
}
