package com.sj.product_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sj.product_service.cache.CacheKeys;
import com.sj.product_service.events.ProductEvents.InventoryEvent;
import com.sj.product_service.events.ProductEvents.ProductLifecycleEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductCacheHandler {

    private final CacheService cacheService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @EventListener
    public void handleProductUpdate(ProductLifecycleEvent event) {
        if (event.getProductId() == null) return;
        String productKey = CacheKeys.product(event.getProductId().toString());
        log.info("Invalidating product cache for {}", productKey);
        cacheService.delete(productKey);

        if (event.getCategoryIds() != null) {
            event.getCategoryIds().forEach(categoryId -> {
                // Page-scoped keys need wildcards; here we clear common first pages
                for (int page = 0; page < 5; page++) {
                    cacheService.delete(CacheKeys.categoryProducts(categoryId.toString(), page));
                }
            });
        }

        if (event.getSellerId() != null) {
            for (int page = 0; page < 5; page++) {
                cacheService.delete(CacheKeys.sellerProducts(event.getSellerId().toString(), page));
            }
        }
    }

    @EventListener
    public void handleInventoryUpdate(InventoryEvent event) {
        if (event.getProductId() == null) return;
        String key = CacheKeys.inventory(event.getProductId().toString());
        try {
            cacheService.set(key, objectMapper.writeValueAsString(event), Duration.ofMinutes(5));
            log.info("Updated inventory cache for {}", key);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize inventory event for cache", e);
        }
    }
}
