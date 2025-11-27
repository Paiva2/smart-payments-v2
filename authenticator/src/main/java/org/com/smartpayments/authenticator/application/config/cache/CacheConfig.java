package org.com.smartpayments.authenticator.application.config.cache;

import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.com.smartpayments.authenticator.core.common.constants.Constants.USER_PROFILE_CACHE_LABEL;

@Configuration
@AllArgsConstructor
@ConditionalOnProperty(value = "spring.cache.enabled", havingValue = "true", matchIfMissing = true)
public class CacheConfig {
    private final RedisConnectionFactory redisConnectionFactory;

    @Bean
    public CacheManager redisCacheManager() {
        RedisSerializationContext.SerializationPair<Object> jsonSerializer =
            RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer());

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .serializeValuesWith(jsonSerializer);

        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        cacheConfigs.put(USER_PROFILE_CACHE_LABEL, defaultConfig.entryTtl(Duration.ofDays(4)));

        return RedisCacheManager.RedisCacheManagerBuilder
            .fromConnectionFactory(redisConnectionFactory)
            .withInitialCacheConfigurations(cacheConfigs)
            .build();
    }
}
