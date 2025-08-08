package com.sj.product_service.service.impl;

import com.sj.product_service.entity.Inventory;
import com.sj.product_service.entity.Product;
import com.sj.product_service.entity.ProductVariant;
import com.sj.product_service.events.ProductEvents;
import com.sj.product_service.repository.InventoryRepository;
import com.sj.product_service.repository.ProductRepository;
import com.sj.product_service.repository.ProductVariantRepository;
import com.sj.product_service.service.CacheService;
import com.sj.product_service.service.EventPublisher;
import com.sj.product_service.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final CacheService cacheService;
    private final EventPublisher eventPublisher;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public Inventory createInventory(UUID productId, UUID variantId, UUID locationId, Integer quantity) {
        log.info("Creating inventory for product: {}, variant: {}, location: {}, quantity: {}", 
                productId, variantId, locationId, quantity);

        // Validate product exists
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        // Validate variant exists if provided
        ProductVariant variant = null;
        if (variantId != null) {
            variant = productVariantRepository.findById(variantId)
                    .orElseThrow(() -> new IllegalArgumentException("Product variant not found: " + variantId));
        }

        // Check if inventory already exists for this combination
        inventoryRepository.findByProductIdAndVariantIdAndLocationId(productId, variantId, locationId)
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Inventory already exists for this product/variant/location combination");
                });

        Inventory inventory = Inventory.builder()
                .product(product)
                .variant(variant)
                .locationId(locationId)
                .quantity(quantity)
                .reservedQuantity(0)
                .reorderLevel(5)
                .maxLevel(null)
                .lastUpdatedAt(LocalDateTime.now())
                .build();

        Inventory savedInventory = inventoryRepository.save(inventory);

        // Publish events and update cache
        publishInventoryCreated(savedInventory);
        updateInventoryCache(savedInventory);

        log.info("Inventory created successfully: {}", savedInventory.getId());
        return savedInventory;
    }

    @Override
    public Inventory updateInventory(UUID inventoryId, Integer quantity) {
        log.info("Updating inventory: {} to quantity: {}", inventoryId, quantity);

        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found: " + inventoryId));

        Integer oldQuantity = inventory.getQuantity();
        inventory.setQuantity(quantity);
        inventory.setLastUpdatedAt(LocalDateTime.now());

        Inventory savedInventory = inventoryRepository.save(inventory);

        // Publish events and update cache
        publishInventoryUpdated(savedInventory, oldQuantity);
        updateInventoryCache(savedInventory);

        log.info("Inventory updated successfully: {} to quantity: {}", inventoryId, quantity);
        return savedInventory;
    }

    @Override
    public Inventory getInventoryById(UUID inventoryId) {
        log.info("Getting inventory by ID: {}", inventoryId);

        return inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found: " + inventoryId));
    }

    @Override
    public List<Inventory> getInventoryByProduct(UUID productId) {
        log.info("Getting inventory for product: {}", productId);

        return inventoryRepository.findByProductId(productId);
    }

    @Override
    public List<Inventory> getInventoryByProductAndLocation(UUID productId, UUID locationId) {
        log.info("Getting inventory for product: {} at location: {}", productId, locationId);

        return inventoryRepository.findByProductIdAndLocationId(productId, locationId);
    }

    @Override
    public Inventory getInventoryByProductAndVariant(UUID productId, UUID variantId) {
        log.info("Getting inventory for product: {} and variant: {}", productId, variantId);

        return inventoryRepository.findByProductIdAndVariantId(productId, variantId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found for product/variant combination"));
    }

    @Override
    public Integer getAvailableQuantity(UUID productId) {
        log.info("Getting available quantity for product: {}", productId);

        Integer totalAvailable = inventoryRepository.getTotalAvailableQuantity(productId);
        return totalAvailable != null ? totalAvailable : 0;
    }

    @Override
    public Integer getAvailableQuantityByLocation(UUID productId, UUID locationId) {
        log.info("Getting available quantity for product: {} at location: {}", productId, locationId);

        List<Inventory> inventories = inventoryRepository.findByProductIdAndLocationId(productId, locationId);
        return inventories.stream()
                .mapToInt(Inventory::getAvailableQuantity)
                .sum();
    }

    @Override
    public List<Inventory> getLowStockInventory(Integer threshold) {
        log.info("Getting low stock inventory with threshold: {}", threshold);

        return inventoryRepository.findLowStockInventoryByThreshold(threshold);
    }

    @Override
    public List<Inventory> getOutOfStockInventory() {
        log.info("Getting out of stock inventory");

        return inventoryRepository.findLowStockInventoryByThreshold(0);
    }

    @Override
    public Page<Inventory> getAllInventory(Pageable pageable) {
        log.info("Getting all inventory with pagination");

        return inventoryRepository.findAll(pageable);
    }

    @Override
    public void reserveStock(UUID productId, UUID variantId, UUID locationId, Integer quantity, UUID userId) {
        log.info("Reserving stock for product: {}, variant: {}, location: {}, quantity: {}", 
                productId, variantId, locationId, quantity);

        Inventory inventory = inventoryRepository.findByProductIdAndVariantIdAndLocationId(productId, variantId, locationId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found for reservation"));

        if (inventory.getAvailableQuantity() < quantity) {
            throw new IllegalStateException("Insufficient stock available. Available: " + inventory.getAvailableQuantity() + ", Requested: " + quantity);
        }

        inventory.setReservedQuantity(inventory.getReservedQuantity() + quantity);
        inventory.setLastUpdatedAt(LocalDateTime.now());
        inventory.setUpdatedBy(userId);

        inventoryRepository.save(inventory);

        // Publish events and update cache
        publishStockReserved(inventory, quantity, userId);
        updateInventoryCache(inventory);

        log.info("Stock reserved successfully: {} units", quantity);
    }

    @Override
    public void releaseStock(UUID productId, UUID variantId, UUID locationId, Integer quantity) {
        log.info("Releasing stock for product: {}, variant: {}, location: {}, quantity: {}", 
                productId, variantId, locationId, quantity);

        Inventory inventory = inventoryRepository.findByProductIdAndVariantIdAndLocationId(productId, variantId, locationId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found for stock release"));

        if (inventory.getReservedQuantity() < quantity) {
            throw new IllegalStateException("Cannot release more than reserved quantity");
        }

        inventory.setReservedQuantity(inventory.getReservedQuantity() - quantity);
        inventory.setLastUpdatedAt(LocalDateTime.now());

        inventoryRepository.save(inventory);

        // Publish events and update cache
        publishStockReleased(inventory, quantity);
        updateInventoryCache(inventory);

        log.info("Stock released successfully: {} units", quantity);
    }

    @Override
    public void updateReservedQuantity(UUID inventoryId, Integer reservedQuantity) {
        log.info("Updating reserved quantity for inventory: {} to {}", inventoryId, reservedQuantity);

        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found: " + inventoryId));

        inventory.setReservedQuantity(reservedQuantity);
        inventory.setLastUpdatedAt(LocalDateTime.now());

        inventoryRepository.save(inventory);

        updateInventoryCache(inventory);
        log.info("Reserved quantity updated successfully: {} to {}", inventoryId, reservedQuantity);
    }

    @Override
    public void restockInventory(UUID inventoryId, Integer quantity, UUID updatedBy) {
        log.info("Restocking inventory: {} with quantity: {}", inventoryId, quantity);

        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found: " + inventoryId));

        Integer oldQuantity = inventory.getQuantity();
        inventory.setQuantity(inventory.getQuantity() + quantity);
        inventory.setLastRestockedAt(LocalDateTime.now());
        inventory.setLastUpdatedAt(LocalDateTime.now());
        inventory.setUpdatedBy(updatedBy);

        inventoryRepository.save(inventory);

        // Publish events and update cache
        publishInventoryRestocked(inventory, oldQuantity, quantity);
        updateInventoryCache(inventory);

        log.info("Inventory restocked successfully: {} with quantity: {}", inventoryId, quantity);
    }

    @Override
    public void updateReorderLevel(UUID inventoryId, Integer reorderLevel) {
        log.info("Updating reorder level for inventory: {} to {}", inventoryId, reorderLevel);

        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found: " + inventoryId));

        inventory.setReorderLevel(reorderLevel);
        inventory.setLastUpdatedAt(LocalDateTime.now());

        inventoryRepository.save(inventory);

        updateInventoryCache(inventory);
        log.info("Reorder level updated successfully: {} to {}", inventoryId, reorderLevel);
    }

    @Override
    public void updateMaxLevel(UUID inventoryId, Integer maxLevel) {
        log.info("Updating max level for inventory: {} to {}", inventoryId, maxLevel);

        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found: " + inventoryId));

        inventory.setMaxLevel(maxLevel);
        inventory.setLastUpdatedAt(LocalDateTime.now());

        inventoryRepository.save(inventory);

        updateInventoryCache(inventory);
        log.info("Max level updated successfully: {} to {}", inventoryId, maxLevel);
    }

    @Override
    public void deleteInventory(UUID inventoryId) {
        log.info("Deleting inventory: {}", inventoryId);

        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found: " + inventoryId));

        inventoryRepository.delete(inventory);

        // Publish events and clear cache
        publishInventoryDeleted(inventory);
        clearInventoryCache(inventory);

        log.info("Inventory deleted successfully: {}", inventoryId);
    }

    @Override
    public boolean hasAvailableStock(UUID productId, Integer quantity) {
        log.info("Checking available stock for product: {} with quantity: {}", productId, quantity);

        Integer availableQuantity = getAvailableQuantity(productId);
        return availableQuantity >= quantity;
    }

    @Override
    public boolean hasAvailableStockByLocation(UUID productId, UUID locationId, Integer quantity) {
        log.info("Checking available stock for product: {} at location: {} with quantity: {}", productId, locationId, quantity);

        Integer availableQuantity = getAvailableQuantityByLocation(productId, locationId);
        return availableQuantity >= quantity;
    }

    @Override
    public List<Inventory> getInventoryByLocation(UUID locationId) {
        log.info("Getting inventory by location: {}", locationId);

        // This would need a custom query in repository
        // For now, returning empty list
        return List.of();
    }

    @Override
    public Integer getTotalQuantity(UUID productId) {
        log.info("Getting total quantity for product: {}", productId);

        List<Inventory> inventories = inventoryRepository.findByProductId(productId);
        return inventories.stream()
                .mapToInt(Inventory::getQuantity)
                .sum();
    }

    @Override
    public Integer getTotalReservedQuantity(UUID productId) {
        log.info("Getting total reserved quantity for product: {}", productId);

        List<Inventory> inventories = inventoryRepository.findByProductId(productId);
        return inventories.stream()
                .mapToInt(Inventory::getReservedQuantity)
                .sum();
    }

    @Override
    public List<Inventory> getInventoryNeedingRestock() {
        log.info("Getting inventory needing restock");

        return inventoryRepository.findLowStockInventory();
    }

    @Override
    public void bulkUpdateInventory(List<Inventory> inventories) {
        log.info("Bulk updating {} inventory records", inventories.size());

        inventoryRepository.saveAll(inventories);

        // Publish events and update cache for each inventory
        inventories.forEach(inventory -> {
            publishInventoryUpdated(inventory, null);
            updateInventoryCache(inventory);
        });

        log.info("Bulk inventory update completed successfully");
    }

    @Override
    public void syncInventoryWithProduct(UUID productId) {
        log.info("Syncing inventory with product: {}", productId);

        // This would sync inventory data with product data
        // Implementation depends on specific business requirements
        log.info("Inventory sync completed for product: {}", productId);
    }

    private void publishInventoryCreated(Inventory inventory) {
        ProductEvents.InventoryEvent event = ProductEvents.InventoryEvent.builder()
                .eventType("INVENTORY_CREATED")
                .productId(inventory.getProduct().getId())
                .variantId(inventory.getVariant() != null ? inventory.getVariant().getId() : null)
                .newQuantity(inventory.getQuantity())
                .locationId(inventory.getLocationId())
                .timestamp(Instant.now())
                .build();
        eventPublisher.publishInventoryChanged(event);
        applicationEventPublisher.publishEvent(event);
    }

    private void publishInventoryUpdated(Inventory inventory, Integer oldQuantity) {
        ProductEvents.InventoryEvent event = ProductEvents.InventoryEvent.builder()
                .eventType("INVENTORY_UPDATED")
                .productId(inventory.getProduct().getId())
                .variantId(inventory.getVariant() != null ? inventory.getVariant().getId() : null)
                .oldQuantity(oldQuantity)
                .newQuantity(inventory.getQuantity())
                .locationId(inventory.getLocationId())
                .timestamp(Instant.now())
                .build();
        eventPublisher.publishInventoryChanged(event);
        applicationEventPublisher.publishEvent(event);
    }

    private void publishInventoryRestocked(Inventory inventory, Integer oldQuantity, Integer restockQuantity) {
        ProductEvents.InventoryEvent event = ProductEvents.InventoryEvent.builder()
                .eventType("INVENTORY_RESTOCKED")
                .productId(inventory.getProduct().getId())
                .variantId(inventory.getVariant() != null ? inventory.getVariant().getId() : null)
                .oldQuantity(oldQuantity)
                .newQuantity(inventory.getQuantity())
                .locationId(inventory.getLocationId())
                .timestamp(Instant.now())
                .build();
        eventPublisher.publishInventoryChanged(event);
        applicationEventPublisher.publishEvent(event);
    }

    private void publishStockReserved(Inventory inventory, Integer quantity, UUID userId) {
        ProductEvents.InventoryEvent event = ProductEvents.InventoryEvent.builder()
                .eventType("STOCK_RESERVED")
                .productId(inventory.getProduct().getId())
                .variantId(inventory.getVariant() != null ? inventory.getVariant().getId() : null)
                .newQuantity(quantity)
                .locationId(inventory.getLocationId())
                .triggeredBy(userId)
                .timestamp(Instant.now())
                .build();
        eventPublisher.publishInventoryChanged(event);
        applicationEventPublisher.publishEvent(event);
    }

    private void publishStockReleased(Inventory inventory, Integer quantity) {
        ProductEvents.InventoryEvent event = ProductEvents.InventoryEvent.builder()
                .eventType("STOCK_RELEASED")
                .productId(inventory.getProduct().getId())
                .variantId(inventory.getVariant() != null ? inventory.getVariant().getId() : null)
                .newQuantity(quantity)
                .locationId(inventory.getLocationId())
                .timestamp(Instant.now())
                .build();
        eventPublisher.publishInventoryChanged(event);
        applicationEventPublisher.publishEvent(event);
    }

    private void publishInventoryDeleted(Inventory inventory) {
        ProductEvents.InventoryEvent event = ProductEvents.InventoryEvent.builder()
                .eventType("INVENTORY_DELETED")
                .productId(inventory.getProduct().getId())
                .variantId(inventory.getVariant() != null ? inventory.getVariant().getId() : null)
                .oldQuantity(inventory.getQuantity())
                .locationId(inventory.getLocationId())
                .timestamp(Instant.now())
                .build();
        eventPublisher.publishInventoryChanged(event);
        applicationEventPublisher.publishEvent(event);
    }

    private void updateInventoryCache(Inventory inventory) {
        // Update inventory cache
        String cacheKey = "inventory:" + inventory.getProduct().getId();
        // In production, you'd serialize the inventory data
        log.info("Updated inventory cache for: {}", cacheKey);
    }

    private void clearInventoryCache(Inventory inventory) {
        // Clear inventory cache
        String cacheKey = "inventory:" + inventory.getProduct().getId();
        log.info("Cleared inventory cache for: {}", cacheKey);
    }
}
