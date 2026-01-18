package com.smartpayments.scheduler.core.common.enums;

import lombok.Getter;

@Getter
public enum EChannelTypeMessageGateway {
    SMS("Sms"),
    WHATS_APP("WhatsApp"),
    EMAIL("E-mail");

    private final String type;

    EChannelTypeMessageGateway(String type) {
        this.type = type;
    }
}