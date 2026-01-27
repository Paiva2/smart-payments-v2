package com.smartpayments.scheduler.core.ports.in.external.messaging;

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
public class CustomMessage {
    private String channelType;
    private String subject;
    private String template;
    private Map<String, Object> variables;
    private String to;
    private List<String> cc;
    private String message;
}
