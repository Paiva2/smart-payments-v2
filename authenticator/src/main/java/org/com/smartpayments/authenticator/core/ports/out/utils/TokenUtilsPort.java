package org.com.smartpayments.authenticator.core.ports.out.utils;

public interface TokenUtilsPort {
    String generateUrlBasedToken(int bytes);
}
