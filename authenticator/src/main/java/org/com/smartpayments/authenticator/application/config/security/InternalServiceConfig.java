package org.com.smartpayments.authenticator.application.config.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "auth.services.internal")
@Getter
@Setter
public class InternalServiceConfig {
    private String secret;
}
