package org.com.smartpayments.authenticator.infra.adapters.dataProvider;

import lombok.AllArgsConstructor;
import org.com.smartpayments.authenticator.core.ports.out.dataProvider.CacheDataProviderPort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@AllArgsConstructor
public class CacheDataProviderAdapter implements CacheDataProviderPort {
    private final StringRedisTemplate redisTemplate;

    @Override
    public boolean existsByKey(String key) {
        return redisTemplate.hasKey(key);
    }

    @Override
    public void persist(String key, String data, Duration duration) {
        redisTemplate.opsForValue().set(key, data, duration);
    }

    @Override
    public String findByKey(String key) {
        return redisTemplate.opsForValue().get(key);
    }
}