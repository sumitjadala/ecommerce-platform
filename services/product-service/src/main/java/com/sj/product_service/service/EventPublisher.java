package com.sj.product_service.service;

import com.sj.product_service.entity.Product;
import com.sj.product_service.events.ProductEvents.InventoryEvent;
import com.sj.product_service.events.ProductEvents.ProductAnalyticsEvent;
import com.sj.product_service.events.ProductEvents.ProductLifecycleEvent;

import java.util.UUID;

public interface EventPublisher {
    void publishProductCreated(Product product);
    void publishProductUpdated(Product before, Product after);
    void publishProductDeleted(Product product);
    void publishProductStatusChanged(UUID productId, String status);

    void publishInventoryChanged(InventoryEvent event);

    void publishProductAnalytics(ProductAnalyticsEvent event);
}
