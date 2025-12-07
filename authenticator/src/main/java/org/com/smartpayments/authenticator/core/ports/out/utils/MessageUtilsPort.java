package org.com.smartpayments.authenticator.core.ports.out.utils;

public interface MessageUtilsPort {
    String generateMessageHash(String issuer);
}
