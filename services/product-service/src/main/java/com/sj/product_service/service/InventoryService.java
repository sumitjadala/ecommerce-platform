package com.sj.product_service.service;

import com.sj.product_service.entity.Inventory;
import com.sj.product_service.entity.Product;

import java.util.UUID;

public interface InventoryService {

    Inventory getInventoryByProductId(UUID productId);

    Inventory adjustStock(UUID productId, int quantityDelta);

    Inventory reserveStock(UUID productId, int quantity);

    Inventory releaseReservedStock(UUID productId, int quantity);

    Inventory fulfillOrder(UUID productId, int quantity);

    Inventory createInventoryForProductWithStock(Product product, int initialStock, int reorderLevel);

    Inventory createInventoryForProduct(Product product);

}
