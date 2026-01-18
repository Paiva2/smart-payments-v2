package com.smartpayments.scheduler.infra.adapters.utils;

import com.smartpayments.scheduler.core.ports.out.utils.MessageUtilsPort;
import org.apache.commons.codec.digest.DigestUtils;
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