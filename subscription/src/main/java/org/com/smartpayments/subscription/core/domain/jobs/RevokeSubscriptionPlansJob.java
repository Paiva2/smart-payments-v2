package org.com.smartpayments.subscription.core.domain.jobs;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.smartpayments.subscription.core.domain.enums.EUserSubscriptionState;
import org.com.smartpayments.subscription.core.domain.model.UserSubscription;
import org.com.smartpayments.subscription.core.ports.in.utils.MessageUtilsPort;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.UserSubscriptionDataProviderPort;
import org.com.smartpayments.subscription.core.ports.out.dto.AsyncMessageOutput;
import org.com.smartpayments.subscription.core.ports.out.dto.AsyncSubscriptionPlanStateInput;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevokeSubscriptionPlansJob {
    private final static ObjectMapper mapper = new ObjectMapper();

    private final UserSubscriptionDataProviderPort userSubscriptionDataProviderPort;

    private final MessageUtilsPort messageUtilsPort;

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${spring.kafka.topics.user-subscription-states}")
    private String userSubscriptionStatesTopic;

    @Scheduled(cron = "0 0 0 * * *") // Runs everyday at midnight (00:00)
    public void run() {
        List<UserSubscription> subscriptionsList = userSubscriptionDataProviderPort.findAllMonthlyToRevoke();

        log.info("[RevokeSubscriptionPlansJob#run - Starting subscription expirations]");

        subscriptionsList.forEach(this::sendMessageToRevoke);

        log.info("[RevokeSubscriptionPlansJob#run - End subscription expirations. Total: {}", subscriptionsList.size());
    }

    private void sendMessageToRevoke(UserSubscription userSubscription) {
        try {
            kafkaTemplate.send(userSubscriptionStatesTopic, mapper.writeValueAsString(input(userSubscription)));
        } catch (Exception e) {
            log.info("[RevokeSubscriptionPlansJob#sendMessageToRevoke - Error while sending message to expire subscription. Error: {}", e.getMessage());
        }
    }

    private AsyncMessageOutput<Object> input(UserSubscription userSubscription) {
        final String issuer = "SUBSCRIPTION";

        final AsyncSubscriptionPlanStateInput renewSubscriptionInput = AsyncSubscriptionPlanStateInput.builder()
            .state(EUserSubscriptionState.EXPIRED)
            .userSubscriptionId(userSubscription.getId())
            .userId(userSubscription.getUser().getId())
            .build();

        return AsyncMessageOutput.builder()
            .messageHash(messageUtilsPort.generateMessageHash(issuer))
            .issuer(issuer)
            .timestamp(new Date())
            .data(renewSubscriptionInput)
            .build();
    }
}
