package com.sj.product_service.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sj.product_service.service.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheServiceImpl implements CacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void set(String key, Object value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl);
        } catch (Exception e) {
            log.warn("Failed to set cache for key {}", key, e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) return null;
            if (clazz.isInstance(value)) {
                return (T) value;
            }
            // Fallback convert
            return objectMapper.convertValue(value, clazz);
        } catch (Exception e) {
            log.warn("Failed to get cache for key {}", key, e);
            return null;
        }
    }

    @Override
    public void delete(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("Failed to delete cache for key {}", key, e);
        }
    }
}
