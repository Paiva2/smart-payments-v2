package org.com.smartpayments.authenticator.core.ports.out.dataProvider;

import java.time.Duration;

public interface CacheDataProviderPort {
    boolean existsByKey(String key);

    void persist(String key, String data, Duration duration);

    String findByKey(String key);
}
