package com.sj.product_service.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sj.product_service.entity.Product;
import com.sj.product_service.events.ProductEvents.InventoryEvent;
import com.sj.product_service.events.ProductEvents.ProductAnalyticsEvent;
import com.sj.product_service.events.ProductEvents.ProductLifecycleEvent;
import com.sj.product_service.service.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaEventPublisher implements EventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void publishProductCreated(Product product) {
        ProductLifecycleEvent event = ProductLifecycleEvent.builder()
                .eventType("CREATED")
                .productId(product.getId())
                .sellerId(product.getSellerId())
                .changes(productToMap(product))
                .timestamp(Instant.now())
                .version(1)
                .build();
        kafkaTemplate.send("product.lifecycle", product.getSellerId().toString(), event);
        log.info("Published product created event for {}", product.getId());
    }

    @Override
    public void publishProductUpdated(Product before, Product after) {
        Map<String, Object> changes = diffProducts(before, after);
        ProductLifecycleEvent event = ProductLifecycleEvent.builder()
                .eventType("UPDATED")
                .productId(after.getId())
                .sellerId(after.getSellerId())
                .changes(changes)
                .timestamp(Instant.now())
                .version(1)
                .build();
        kafkaTemplate.send("product.lifecycle", after.getSellerId().toString(), event);
        log.info("Published product updated event for {}", after.getId());
    }

    @Override
    public void publishProductDeleted(Product product) {
        ProductLifecycleEvent event = ProductLifecycleEvent.builder()
                .eventType("DELETED")
                .productId(product.getId())
                .sellerId(product.getSellerId())
                .timestamp(Instant.now())
                .version(1)
                .build();
        kafkaTemplate.send("product.lifecycle", product.getSellerId().toString(), event);
        log.info("Published product deleted event for {}", product.getId());
    }

    @Override
    public void publishProductStatusChanged(UUID productId, String status) {
        ProductLifecycleEvent event = ProductLifecycleEvent.builder()
                .eventType("STATUS_CHANGED")
                .productId(productId)
                .changes(Map.of("status", status))
                .timestamp(Instant.now())
                .version(1)
                .build();
        kafkaTemplate.send("product.lifecycle", productId.toString(), event);
        log.info("Published product status changed event for {}", productId);
    }

    @Override
    public void publishInventoryChanged(InventoryEvent event) {
        kafkaTemplate.send("inventory.changes", event.getProductId().toString(), event);
        log.info("Published inventory event {} for {}", event.getEventType(), event.getProductId());
    }

    @Override
    public void publishProductAnalytics(ProductAnalyticsEvent event) {
        kafkaTemplate.send("product.analytics", event.getProductId().toString(), event);
        log.info("Published analytics event {} for {}", event.getEventType(), event.getProductId());
    }

    private Map<String, Object> productToMap(Product product) {
        return objectMapper.convertValue(product, Map.class);
    }

    private Map<String, Object> diffProducts(Product before, Product after) {
        Map<String, Object> beforeMap = productToMap(before);
        Map<String, Object> afterMap = productToMap(after);
        Map<String, Object> changes = new HashMap<>();
        afterMap.forEach((k, v) -> {
            Object oldVal = beforeMap.get(k);
            if ((oldVal == null && v != null) || (oldVal != null && !oldVal.equals(v))) {
                changes.put(k, v);
            }
        });
        return changes;
    }
}
