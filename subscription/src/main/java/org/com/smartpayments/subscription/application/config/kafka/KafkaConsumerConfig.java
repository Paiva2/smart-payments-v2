package org.com.smartpayments.subscription.application.config.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Bean
    public ConsumerFactory<String, String> defaultConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> defaultContainerFactory(ConsumerFactory<String, String> defaultConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(defaultConsumerFactory);

        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> topicWithDltContainerFactory(
        ConsumerFactory<String, String> defaultConsumerFactory,
        KafkaTemplate<String, String> kafkaTemplate
    ) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate, (record, exception) -> {
            String originalTopic = record.topic();
            String topicDlt = originalTopic.concat(".dlt");
            return new TopicPartition(topicDlt, record.partition());
        });

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer);

        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setCommonErrorHandler(errorHandler);
        factory.setConsumerFactory(defaultConsumerFactory);

        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> topicWithDltFixedRetryContainerFactory(
        ConsumerFactory<String, String> defaultConsumerFactory,
        KafkaTemplate<String, String> kafkaTemplate
    ) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate, (record, exception) -> {
            String originalTopic = record.topic();
            String topicDlt = originalTopic.concat(".dlt");
            return new TopicPartition(topicDlt, record.partition());
        });

        final int maxAttempts = 5;
        final long interval = 5000L;

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
            recoverer,
            new FixedBackOff(interval, maxAttempts)
        );

        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setCommonErrorHandler(errorHandler);
        factory.setConsumerFactory(defaultConsumerFactory);

        return factory;
    }
}
