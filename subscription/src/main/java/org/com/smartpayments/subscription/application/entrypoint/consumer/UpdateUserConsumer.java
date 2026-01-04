package org.com.smartpayments.subscription.application.entrypoint.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.smartpayments.subscription.core.domain.usecase.user.updateUser.UpdateUserUsecase;
import org.com.smartpayments.subscription.core.ports.in.dto.AsyncMessageInput;
import org.com.smartpayments.subscription.core.ports.in.dto.AsyncUpdateUserInput;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static java.util.Objects.isNull;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateUserConsumer {
    private final static ObjectMapper mapper = new ObjectMapper();

    private final UpdateUserUsecase updateUserUsecase;

    @KafkaListener(
        id = "updateUserConsumer",
        topics = "${spring.kafka.topics.update-user}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "topicWithDltContainerFactory"
    )
    public void execute(String message) {
        try {
            AsyncMessageInput<AsyncUpdateUserInput> input = convertBody(message);

            log.info("[UpdateUserConsumer#execute] new message on consumer: {}", message);

            if (isNull(input) || isNull(input.getData())) {
                log.error("[UpdateUserConsumer#execute] message without data will be discarded: {}", message);
                return;
            }

            AsyncUpdateUserInput updateUserInput = input.getData();
            updateUserUsecase.execute(updateUserInput);
        } catch (Exception e) {
            log.error("[UpdateUserConsumer#execute] - Error while consuming message: {}", message, e);
            throw e;
        }
    }

    private AsyncMessageInput<AsyncUpdateUserInput> convertBody(String message) {
        try {
            return mapper.readValue(message,
                new TypeReference<AsyncMessageInput<AsyncUpdateUserInput>>() {
                }
            );
        } catch (JsonProcessingException e) {
            log.error("[UpdateUserConsumer#convertBody] - Error while converting message: {}", e.getMessage());
            return null;
        }
    }
}
