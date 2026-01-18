package org.com.smartpayments.subscription.core.ports.out.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConsumeUserCreditsInternalOutput {
    private final static ObjectMapper mapper = new ObjectMapper();

    private Long userId;
    private String sms;
    private String email;
    private String whatsapp;

    public static ConsumeUserCreditsInternalOutput getResultAsObject(String result) {
        try {
            return mapper.readValue(result, ConsumeUserCreditsInternalOutput.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Invalid JSON stored", e);
        }
    }
}
