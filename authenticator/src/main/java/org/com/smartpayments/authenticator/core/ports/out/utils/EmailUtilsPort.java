package org.com.smartpayments.authenticator.core.ports.out.utils;

import org.com.smartpayments.authenticator.core.ports.in.dto.SendEmailInput;

public interface EmailUtilsPort {
    void sendEmail(SendEmailInput input);
}
