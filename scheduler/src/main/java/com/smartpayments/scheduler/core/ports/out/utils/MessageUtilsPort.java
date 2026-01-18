package com.smartpayments.scheduler.core.ports.out.utils;

public interface MessageUtilsPort {
    String generateMessageHash(String issuer);
}