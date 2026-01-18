package com.smartpayments.scheduler.application.config.http;

import com.smartpayments.scheduler.core.common.exception.SubscriptionClientErrorException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.List;

@Configuration
public class RetryTemplateConfig {
    @Bean
    public RetryTemplate generalRetryTemplateSubscriptionService() {
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000); // initial interval 1s
        backOffPolicy.setMultiplier(2.0); // 2x each retry
        backOffPolicy.setMaxInterval(7000); // max interval 7s

        return RetryTemplate.builder()
            .customBackoff(backOffPolicy)
            .maxAttempts(3)
            .notRetryOn(List.of(SubscriptionClientErrorException.class, RuntimeException.class))
            .build();
    }
}