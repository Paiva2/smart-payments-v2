package org.com.smartpayments.subscription.application.entrypoint.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.smartpayments.subscription.core.domain.usecase.user.newUser.NewUserUsecase;
import org.com.smartpayments.subscription.core.ports.in.dto.AsyncMessageInput;
import org.com.smartpayments.subscription.core.ports.in.dto.AsyncNewUserInput;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static java.util.Objects.isNull;

@Slf4j
@Component
@AllArgsConstructor
public class NewUserConsumer {
    private final static ObjectMapper mapper = new ObjectMapper();

    private final NewUserUsecase newUserUsecase;

    @KafkaListener(
        id = "newUserConsumer",
        topics = "${spring.kafka.topics.new-user}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "topicWithDltContainerFactory"
    )
    public void execute(String message) {
        try {
            AsyncMessageInput<AsyncNewUserInput> input = convertBody(message);

            log.info("[NewUserConsumer#execute] new message on consumer: {}", message);

            if (isNull(input) || isNull(input.getData())) {
                log.error("[NewUserConsumer#execute] message without data will be discarded: {}", message);
                return;
            }

            AsyncNewUserInput newUserInput = input.getData();
            newUserUsecase.execute(newUserInput);
        } catch (Exception e) {
            log.error("[NewUserConsumer#execute] - Error while consuming message: {}", message, e);
            throw e;
        }
    }

    private AsyncMessageInput<AsyncNewUserInput> convertBody(String message) {
        try {
            return mapper.readValue(message,
                new TypeReference<AsyncMessageInput<AsyncNewUserInput>>() {
                }
            );
        } catch (JsonProcessingException e) {
            log.error("[NewUserConsumer#convertBody] - Error while converting message: {}", e.getMessage());
            return null;
        }
    }
}
