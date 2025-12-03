package org.com.smartpayments.subscription.application.config;

import org.com.smartpayments.subscription.core.domain.common.exception.PaymentGatewayClientErrorException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.List;

@Configuration
public class RetryTemplateConfig {
    @Bean
    public RetryTemplate generalRetryTemplatePaymentGateway() {
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000); // initial interval 1s
        backOffPolicy.setMultiplier(2.0); // 2x each retry
        backOffPolicy.setMaxInterval(10000); // max interval 10s

        return RetryTemplate.builder()
            .customBackoff(backOffPolicy)
            .maxAttempts(3)
            .notRetryOn(List.of(PaymentGatewayClientErrorException.class))
            .build();
    }
}