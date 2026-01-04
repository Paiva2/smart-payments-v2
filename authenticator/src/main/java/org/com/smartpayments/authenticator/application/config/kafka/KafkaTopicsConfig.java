package org.com.smartpayments.authenticator.application.config.kafka;

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

    @Value("${spring.kafka.topics.mail-sender}")
    private String mailSenderTopic;

    @Value("${spring.kafka.topics.mail-sender-dlt}")
    private String mailSenderDltTopic;

    @Value("${spring.kafka.topics.new-user}")
    private String newUserTopic;

    @Value("${spring.kafka.topics.new-user-dlt}")
    private String newUserTopicDlt;

    @Value("${spring.kafka.topics.update-user}")
    private String updateUserTopic;

    @Value("${spring.kafka.topics.update-user-dlt}")
    private String updateUserTopicDlt;

    @Bean
    public KafkaAdmin admin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);

        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic mailSenderTopic() {
        Map<String, String> topicConfigs = new HashMap<>() {{
            put(TopicConfig.RETENTION_MS_CONFIG, MESSAGES_RETENTION_DEFAULT_MILLIS_THREE_DAYS);
        }};

        return mountTopic(mailSenderTopic, DEFAULT_PARTITION_COUNT, DEFAULT_REPLICAS_COUNT, topicConfigs);
    }

    @Bean
    public NewTopic mailSenderDltTopic() {
        Map<String, String> topicConfigs = new HashMap<>() {{
            put(TopicConfig.RETENTION_MS_CONFIG, "-1");
        }};

        return mountTopic(mailSenderDltTopic, DEFAULT_PARTITION_COUNT, DEFAULT_REPLICAS_COUNT, topicConfigs);
    }

    @Bean
    public NewTopic newUserTopic() {
        Map<String, String> topicConfigs = new HashMap<>() {{
            put(TopicConfig.RETENTION_MS_CONFIG, "-1");
        }};

        return mountTopic(newUserTopic, DEFAULT_PARTITION_COUNT, DEFAULT_REPLICAS_COUNT, topicConfigs);
    }

    @Bean
    public NewTopic newUserTopicDlt() {
        Map<String, String> topicConfigs = new HashMap<>() {{
            put(TopicConfig.RETENTION_MS_CONFIG, "-1");
        }};

        return mountTopic(newUserTopicDlt, DEFAULT_PARTITION_COUNT, DEFAULT_REPLICAS_COUNT, topicConfigs);
    }

    @Bean
    public NewTopic updateUserTopic() {
        Map<String, String> topicConfigs = new HashMap<>() {{
            put(TopicConfig.RETENTION_MS_CONFIG, "-1");
        }};

        return mountTopic(updateUserTopic, DEFAULT_PARTITION_COUNT, DEFAULT_REPLICAS_COUNT, topicConfigs);
    }

    @Bean
    public NewTopic updateUserTopicDlt() {
        Map<String, String> topicConfigs = new HashMap<>() {{
            put(TopicConfig.RETENTION_MS_CONFIG, "-1");
        }};

        return mountTopic(updateUserTopicDlt, DEFAULT_PARTITION_COUNT, DEFAULT_REPLICAS_COUNT, topicConfigs);
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
