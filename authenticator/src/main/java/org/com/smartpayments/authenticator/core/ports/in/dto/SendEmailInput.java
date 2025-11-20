package org.com.smartpayments.authenticator.core.ports.in.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendEmailInput {
    private String to;
    private String subject;
    private List<String> cc;
    private Map<String, Object> variables;
    private String templateName;
}
