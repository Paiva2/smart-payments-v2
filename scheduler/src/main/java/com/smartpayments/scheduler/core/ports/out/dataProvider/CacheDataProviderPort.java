package com.smartpayments.scheduler.core.ports.out.dataProvider;

import java.time.Duration;

public interface CacheDataProviderPort {
    boolean existsByKey(String key);

    void persist(String key, String data, Duration duration);

}
