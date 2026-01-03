package org.com.smartpayments.subscription.application.config.kafka;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaTopicsConfig {
    private final static Integer DEFAULT_PARTITION_COUNT = 3;
    private final static Integer DEFAULT_REPLICAS_COUNT = 1;
    private final static String MESSAGES_RETENTION_DEFAULT_MILLIS_THREE_DAYS = "259200000";

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;

    @Value("${spring.kafka.topics.user-subscription-update}")
    private String userSubscriptionUpdateTopic;

    @Value("${spring.kafka.topics.user-subscription-update-dlt}")
    private String userSubscriptionUpdateTopicDlt;

    @Value("${spring.kafka.topics.user-subscription-states}")
    private String userSubscriptionStatesTopic;

    @Value("${spring.kafka.topics.user-subscription-states-dlt}")
    private String userSubscriptionStatesTopicDlt;

    @Value("${spring.kafka.topics.payment-events}")
    private String purchaseChargeEventsTopic;

    @Value("${spring.kafka.topics.payment-events-dlt}")
    private String purchaseChargeEventsTopicDlt;

    @Bean
    public KafkaAdmin admin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);

        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic userSubscriptionUpdateTopic() {
        Map<String, String> topicConfigs = new HashMap<>() {{
            put(TopicConfig.RETENTION_MS_CONFIG, MESSAGES_RETENTION_DEFAULT_MILLIS_THREE_DAYS);
        }};

        return mountTopic(userSubscriptionUpdateTopic, DEFAULT_PARTITION_COUNT, DEFAULT_REPLICAS_COUNT, topicConfigs);
    }

    @Bean
    public NewTopic userSubscriptionUpdateTopicDlt() {
        Map<String, String> topicConfigs = new HashMap<>() {{
            put(TopicConfig.RETENTION_MS_CONFIG, "-1");
        }};

        return mountTopic(userSubscriptionUpdateTopicDlt, DEFAULT_PARTITION_COUNT, DEFAULT_REPLICAS_COUNT, topicConfigs);
    }

    @Bean
    public NewTopic userSubscriptionStatesTopic() {
        Map<String, String> topicConfigs = new HashMap<>() {{
            put(TopicConfig.RETENTION_MS_CONFIG, MESSAGES_RETENTION_DEFAULT_MILLIS_THREE_DAYS);
        }};

        return mountTopic(userSubscriptionStatesTopic, DEFAULT_PARTITION_COUNT, DEFAULT_REPLICAS_COUNT, topicConfigs);
    }

    @Bean
    public NewTopic userSubscriptionStatesTopicDlt() {
        Map<String, String> topicConfigs = new HashMap<>() {{
            put(TopicConfig.RETENTION_MS_CONFIG, "-1");
        }};

        return mountTopic(userSubscriptionStatesTopicDlt, DEFAULT_PARTITION_COUNT, DEFAULT_REPLICAS_COUNT, topicConfigs);
    }

    @Bean
    public NewTopic purchaseChargeEventsTopic() {
        Map<String, String> topicConfigs = new HashMap<>() {{
            put(TopicConfig.RETENTION_MS_CONFIG, MESSAGES_RETENTION_DEFAULT_MILLIS_THREE_DAYS);
        }};

        return mountTopic(purchaseChargeEventsTopic, DEFAULT_PARTITION_COUNT, DEFAULT_REPLICAS_COUNT, topicConfigs);
    }

    @Bean
    public NewTopic purchaseChargeEventsTopicDlt() {
        Map<String, String> topicConfigs = new HashMap<>() {{
            put(TopicConfig.RETENTION_MS_CONFIG, "-1");
        }};

        return mountTopic(purchaseChargeEventsTopicDlt, DEFAULT_PARTITION_COUNT, DEFAULT_REPLICAS_COUNT, topicConfigs);
    }

    private NewTopic mountTopic(String topicName, int partitions, int replicas, Map<String, String> config) {
        return TopicBuilder
            .name(topicName)
            .partitions(partitions)
            .replicas(replicas)
            .configs(config)
            .build();
    }
}