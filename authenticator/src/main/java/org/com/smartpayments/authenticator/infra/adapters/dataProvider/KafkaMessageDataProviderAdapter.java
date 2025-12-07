package org.com.smartpayments.authenticator.infra.adapters.dataProvider;

import lombok.AllArgsConstructor;
import org.com.smartpayments.authenticator.core.ports.out.dataProvider.AsyncMessageDataProviderPort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class KafkaMessageDataProviderAdapter implements AsyncMessageDataProviderPort {
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Override
    public void sendMessage(String destination, Object data) {
        kafkaTemplate.send(destination, (String) data);
    }

    @Override
    public void sendMessage(String destination, String key, Object data) {
        kafkaTemplate.send(destination, (String) data);
    }
}
