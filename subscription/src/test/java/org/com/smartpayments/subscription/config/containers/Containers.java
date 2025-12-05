package org.com.smartpayments.subscription.config.containers;

import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Random;

public class Containers {
    public static PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16")
        .withDatabaseName("subscription-db")
        .withUsername("test")
        .withPassword("test")
        .withReuse(true)
        .withCreateContainerCmdModifier(cmd -> cmd.withName("subscription-db-pg-" + (new Random().nextInt() + Integer.MAX_VALUE)));
}