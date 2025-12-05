package org.com.smartpayments.subscription.core.ports.out.utils;

import com.auth0.jwt.interfaces.DecodedJWT;

public interface JwtUtilsPort {
    DecodedJWT verifyAuthJwt(String authToken);
}
