package org.com.smartpayments.subscription.core.ports.out.dataprovider;

import java.time.Duration;

public interface CacheDataProviderPort {
    boolean existsByKey(String key);

    void persist(String key, String data, Duration duration);

    String findByKey(String key);
}
