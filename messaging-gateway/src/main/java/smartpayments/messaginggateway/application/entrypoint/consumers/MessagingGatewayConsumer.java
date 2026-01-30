package smartpayments.messaginggateway.application.entrypoint.consumers;

import com.messaging_gateway.avro.CustomMessage;
import com.messaging_gateway.avro.CustomMessageKey;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import smartpayments.messaginggateway.core.domain.usecase.email.sendEmail.SendEmailUsecase;
import smartpayments.messaginggateway.core.ports.in.usecase.SendEmailInput;

import java.util.List;

import static java.util.Objects.nonNull;

@Slf4j
@Component
@AllArgsConstructor
public class MessagingGatewayConsumer {
    private final static String AVRO_SCHEMA_NAME = "CustomMessage";

    private final SendEmailUsecase sendEmailUsecase;

    @KafkaListener(
        topics = "${spring.kafka.topics.messaging-gateway}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "topicWithDltFixedRetryContainerFactory"
    )
    public void consume(ConsumerRecord<CustomMessageKey, CustomMessage> specificRecord) {
        log.info("[MessagingGatewayConsumer#consume] - New message on messaging gateway {}", specificRecord.value().getSchema().getName());

        if (specificRecord.value().getSchema().getName().equals(AVRO_SCHEMA_NAME)) {
            CustomMessage message = specificRecord.value();

            switch (String.valueOf(message.getChannelType())) {
                case "EMAIL" -> {
                    SendEmailInput input = SendEmailInput.builder()
                        .to(message.getTo().toString())
                        .subject(message.getSubject().toString())
                        .template(message.getTemplate().toString())
                        .cc(nonNull(message.getCc()) ? message.getCc().stream().map(CharSequence::toString).toList() : List.of())
                        .variables(message.getVariables())
                        .build();
                    sendEmailUsecase.execute(input);
                }
                case "WHATS_APP" -> {
                    log.info("[MessagingGatewayConsumer#consume] - Channel type: WhatsApp not implemented yet.");
                }
                case "SMS" -> {
                    log.info("[MessagingGatewayConsumer#consume] - Channel type: SMS not implemented yet.");
                }
                default -> {
                    log.error("[MessagingGatewayConsumer#consume] - Invalid channel type: {}", message.getChannelType());
                }
            }
        }
    }
}
