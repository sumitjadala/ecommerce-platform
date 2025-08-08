package com.sj.product_service.service;

import java.time.Duration;

public interface CacheService {
    void set(String key, Object value, Duration ttl);
    <T> T get(String key, Class<T> clazz);
    void delete(String key);
}
