package com.smartpayments.scheduler.application.config.kafka;

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

    @Value("${spring.kafka.topics.process-scheduled-payment-notifications}")
    private String processNotificationTopic;

    @Value("${spring.kafka.topics.process-scheduled-payment-notifications-dlt}")
    private String processNotificationTopicDlt;

    @Bean
    public KafkaAdmin admin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);

        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic processNotificationTopic() {
        Map<String, String> topicConfigs = new HashMap<>() {{
            put(TopicConfig.RETENTION_MS_CONFIG, MESSAGES_RETENTION_DEFAULT_MILLIS_THREE_DAYS);
        }};

        return mountTopic(processNotificationTopic, DEFAULT_PARTITION_COUNT, DEFAULT_REPLICAS_COUNT, topicConfigs);
    }

    @Bean
    public NewTopic processNotificationTopicDlt() {
        Map<String, String> topicConfigs = new HashMap<>() {{
            put(TopicConfig.RETENTION_MS_CONFIG, "-1");
        }};

        return mountTopic(processNotificationTopicDlt, DEFAULT_PARTITION_COUNT, DEFAULT_REPLICAS_COUNT, topicConfigs);
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