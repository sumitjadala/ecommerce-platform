package com.sj.product_service.service;

import com.sj.product_service.entity.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface InventoryService {

    Inventory createInventory(UUID productId, UUID variantId, UUID locationId, Integer quantity);

    Inventory updateInventory(UUID inventoryId, Integer quantity);

    Inventory getInventoryById(UUID inventoryId);

    List<Inventory> getInventoryByProduct(UUID productId);

    List<Inventory> getInventoryByProductAndLocation(UUID productId, UUID locationId);

    Inventory getInventoryByProductAndVariant(UUID productId, UUID variantId);

    Integer getAvailableQuantity(UUID productId);

    Integer getAvailableQuantityByLocation(UUID productId, UUID locationId);

    List<Inventory> getLowStockInventory(Integer threshold);

    List<Inventory> getOutOfStockInventory();

    Page<Inventory> getAllInventory(Pageable pageable);

    void reserveStock(UUID productId, UUID variantId, UUID locationId, Integer quantity, UUID userId);

    void releaseStock(UUID productId, UUID variantId, UUID locationId, Integer quantity);

    void updateReservedQuantity(UUID inventoryId, Integer reservedQuantity);

    void restockInventory(UUID inventoryId, Integer quantity, UUID updatedBy);

    void updateReorderLevel(UUID inventoryId, Integer reorderLevel);

    void updateMaxLevel(UUID inventoryId, Integer maxLevel);

    void deleteInventory(UUID inventoryId);

    boolean hasAvailableStock(UUID productId, Integer quantity);

    boolean hasAvailableStockByLocation(UUID productId, UUID locationId, Integer quantity);

    List<Inventory> getInventoryByLocation(UUID locationId);

    Integer getTotalQuantity(UUID productId);

    Integer getTotalReservedQuantity(UUID productId);

    List<Inventory> getInventoryNeedingRestock();

    void bulkUpdateInventory(List<Inventory> inventories);

    void syncInventoryWithProduct(UUID productId);
}
