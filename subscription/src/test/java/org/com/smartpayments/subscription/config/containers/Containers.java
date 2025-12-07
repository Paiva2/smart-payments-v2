package org.com.smartpayments.subscription.config.containers;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.kafka.ConfluentKafkaContainer;

import java.util.Random;

public class Containers {
    public static PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16")
        .withDatabaseName("subscription-db")
        .withUsername("test")
        .withPassword("test")
        .withReuse(true)
        .withCreateContainerCmdModifier(cmd -> cmd.withName("subscription-db-pg-" + (new Random().nextInt() + Integer.MAX_VALUE)));

    public static final ConfluentKafkaContainer KAFKA = new ConfluentKafkaContainer("confluentinc/cp-kafka:7.8.0")
        .withReuse(true)
        .withCreateContainerCmdModifier(cmd -> cmd.withName("subscription-kafka-" + (new Random().nextInt() + Integer.MAX_VALUE)));
}