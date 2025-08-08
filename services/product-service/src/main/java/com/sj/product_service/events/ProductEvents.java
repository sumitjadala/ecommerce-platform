package com.sj.product_service.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ProductEvents {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductLifecycleEvent {
        private String eventType; // CREATED, UPDATED, DELETED, STATUS_CHANGED
        private UUID productId;
        private UUID sellerId;
        private Map<String, Object> changes;
        private Instant timestamp;
        private int version;
        private List<UUID> categoryIds;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InventoryEvent {
        private String eventType; // STOCK_UPDATED, LOW_STOCK, OUT_OF_STOCK, RESTOCKED
        private UUID productId;
        private UUID variantId;
        private Integer oldQuantity;
        private Integer newQuantity;
        private UUID locationId;
        private UUID triggeredBy;
        private Instant timestamp;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductAnalyticsEvent {
        private String eventType; // VIEW, SEARCH, ADD_TO_CART, PURCHASE, REVIEW
        private UUID productId;
        private UUID userId;
        private Map<String, Object> metadata;
        private Instant timestamp;
    }
}
