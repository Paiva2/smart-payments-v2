package smartpayments.messaginggateway.application.entrypoint.consumers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class MessagingGatewayConsumer {
    private final static String AVRO_SCHEMA_NAME = "CustomMessage";

    @KafkaListener(
        topics = "${spring.kafka.topics.messaging-gateway}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "topicWithDltFixedRetryContainerFactory"
    )
    public void consume(SpecificRecord specificRecord) {
        log.info("[MessagingGatewayConsumer#consume] - New message on messaging gateway {}", specificRecord.getSchema().getName());
        
        if (specificRecord.getSchema().getName().equals(AVRO_SCHEMA_NAME)) {

        }
    }
}
