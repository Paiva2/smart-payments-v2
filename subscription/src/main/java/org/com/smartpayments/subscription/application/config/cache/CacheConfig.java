package org.com.smartpayments.subscription.application.config.cache;

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
            .entryTtl(Duration.ofDays(7)) // ttl default 7 days
            .serializeValuesWith(jsonSerializer);

        return RedisCacheManager.RedisCacheManagerBuilder
            .fromConnectionFactory(redisConnectionFactory)
            .cacheDefaults(defaultConfig)
            .build();
    }
}
