package org.com.smartpayments.authenticator.core.ports.out.utils;

public interface PasswordUtilsPort {
    String hashPassword(String password);

    boolean comparePassword(String rawPassword, String hash);
}
