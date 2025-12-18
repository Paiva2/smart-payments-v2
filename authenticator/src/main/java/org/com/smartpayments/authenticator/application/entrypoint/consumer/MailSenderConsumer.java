package org.com.smartpayments.authenticator.application.entrypoint.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.smartpayments.authenticator.core.ports.in.dto.SendEmailInput;
import org.com.smartpayments.authenticator.core.ports.out.utils.EmailUtilsPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MailSenderConsumer {
    private final static ObjectMapper mapper = new ObjectMapper();

    private final EmailUtilsPort emailUtilsPort;

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${spring.kafka.topics.mail-sender-dlt}")
    private String mailSenderDlt;

    // must do: create a service to this
    @KafkaListener(
        topics = "${spring.kafka.topics.mail-sender}",
        groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(String message) {
        log.info("Received mail sender message: {}", message);

        try {
            SendEmailInput data = convertMessageData(message);
            emailUtilsPort.sendEmail(data);
        } catch (JsonProcessingException e) {
            log.error("Error while converting message data. Message: {} | Error: {}", message, e.getMessage());
        } catch (Exception e) {
            log.error("Unhandled error while sending mail. Message: {} | Error: {}", message, e.getMessage());
            kafkaTemplate.send(mailSenderDlt, message);
        }
    }

    private SendEmailInput convertMessageData(String messageStr) throws JsonProcessingException {
        return mapper.readValue(messageStr, SendEmailInput.class);
    }
}
