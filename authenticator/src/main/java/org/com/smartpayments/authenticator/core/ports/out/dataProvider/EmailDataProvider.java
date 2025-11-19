package org.com.smartpayments.authenticator.core.ports.out.dataProvider;

import java.util.Map;

public interface EmailDataProvider {
    void sendEmail(String to, String subject, String template, Map<String, Object> variables);
}
