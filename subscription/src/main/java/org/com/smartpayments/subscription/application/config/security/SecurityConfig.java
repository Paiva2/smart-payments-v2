package org.com.smartpayments.subscription.application.config.security;

import jakarta.servlet.DispatcherType;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfig {
    private final SecurityFilterConfig securityFilterConfig;
    private final ServiceSignatureValidator serviceSignatureValidator;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(req -> {
            req.dispatcherTypeMatchers(DispatcherType.ERROR).permitAll();
            req.requestMatchers(publicEndpoints()).permitAll();
            req.requestMatchers(internalEndpoints()).access(serviceSignatureValidator);
            req.anyRequest().hasAnyRole("MEMBER", "ADMIN");
        });

        http.cors(AbstractHttpConfigurer::disable) // todo:
            .csrf(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable);

        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.addFilterBefore(securityFilterConfig, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private RequestMatcher publicEndpoints() {
        return new OrRequestMatcher(
            securityFilterConfig.NON_FILTERABLE_ENDPOINTS.getFirst()
        );
    }

    private RequestMatcher internalEndpoints() {
        return new OrRequestMatcher(
            securityFilterConfig.NON_FILTERABLE_ENDPOINTS_INTERNAL.getFirst(),
            securityFilterConfig.NON_FILTERABLE_ENDPOINTS_INTERNAL.get(1)
        );
    }
}