package org.com.smartpayments.subscription.core.ports.in.utils;

public interface MessageUtilsPort {
    String generateMessageHash(String issuer);
}