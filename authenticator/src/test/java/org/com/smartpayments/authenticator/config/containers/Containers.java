package org.com.smartpayments.authenticator.config.containers;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Random;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

public class Containers {
    public static PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16")
        .withDatabaseName("auth-db")
        .withUsername("test")
        .withPassword("test")
        .withReuse(true)
        .withCreateContainerCmdModifier(cmd -> cmd.withName("auth-db-pg-" + (new Random().nextInt() + Integer.MAX_VALUE)));

    public static final ConfluentKafkaContainer KAFKA = new ConfluentKafkaContainer("confluentinc/cp-kafka:7.8.0")
        .withReuse(true)
        .withCreateContainerCmdModifier(cmd -> cmd.withName("auth-kafka-" + (new Random().nextInt() + Integer.MAX_VALUE)));

    public static final LocalStackContainer LOCALSTACK = new LocalStackContainer(DockerImageName.parse("localstack/localstack"))
        .withServices(S3)
        .withReuse(true)
        .withCreateContainerCmdModifier(cmd -> cmd.withName("auth-localstack-" + (new Random().nextInt() + Integer.MAX_VALUE)));
}