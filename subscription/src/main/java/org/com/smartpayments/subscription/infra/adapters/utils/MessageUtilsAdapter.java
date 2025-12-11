package org.com.smartpayments.subscription.infra.adapters.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.com.smartpayments.subscription.core.ports.in.utils.MessageUtilsPort;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

@Component
public class MessageUtilsAdapter implements MessageUtilsPort {
    @Override
    public String generateMessageHash(String issuer) {
        return DigestUtils.md5Hex(String.format("%s_%s_%s", issuer, UUID.randomUUID(), new Date().getTime()));
    }
}