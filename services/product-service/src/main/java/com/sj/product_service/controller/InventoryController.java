package com.sj.product_service.controller;

import com.sj.product_service.entity.Inventory;
import com.sj.product_service.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Inventory Management", description = "APIs for managing product inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SELLER')")
    @Operation(summary = "Create inventory", description = "Creates new inventory for a product")
    public ResponseEntity<Inventory> createInventory(
            @RequestParam UUID productId,
            @RequestParam(required = false) UUID variantId,
            @RequestParam(required = false) UUID locationId,
            @RequestParam Integer quantity) {

        log.info("Creating inventory for product: {}, variant: {}, location: {}, quantity: {}", 
                productId, variantId, locationId, quantity);

        Inventory inventory = inventoryService.createInventory(productId, variantId, locationId, quantity);
        return ResponseEntity.status(HttpStatus.CREATED).body(inventory);
    }

    @GetMapping
    @Operation(summary = "Get all inventory", description = "Retrieves all inventory with pagination")
    public ResponseEntity<Page<Inventory>> getAllInventory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("Getting all inventory with page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<Inventory> inventory = inventoryService.getAllInventory(pageable);
        return ResponseEntity.ok(inventory);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get inventory by ID", description = "Retrieves specific inventory by ID")
    public ResponseEntity<Inventory> getInventory(@PathVariable UUID id) {
        log.info("Getting inventory by ID: {}", id);

        Inventory inventory = inventoryService.getInventoryById(id);
        return ResponseEntity.ok(inventory);
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get inventory by product", description = "Retrieves all inventory for a product")
    public ResponseEntity<List<Inventory>> getInventoryByProduct(@PathVariable UUID productId) {
        log.info("Getting inventory for product: {}", productId);

        List<Inventory> inventory = inventoryService.getInventoryByProduct(productId);
        return ResponseEntity.ok(inventory);
    }

    @GetMapping("/product/{productId}/location/{locationId}")
    @Operation(summary = "Get inventory by product and location", description = "Retrieves inventory for a product at specific location")
    public ResponseEntity<List<Inventory>> getInventoryByProductAndLocation(
            @PathVariable UUID productId,
            @PathVariable UUID locationId) {

        log.info("Getting inventory for product: {} at location: {}", productId, locationId);

        List<Inventory> inventory = inventoryService.getInventoryByProductAndLocation(productId, locationId);
        return ResponseEntity.ok(inventory);
    }

    @GetMapping("/product/{productId}/variant/{variantId}")
    @Operation(summary = "Get inventory by product and variant", description = "Retrieves inventory for a product variant")
    public ResponseEntity<Inventory> getInventoryByProductAndVariant(
            @PathVariable UUID productId,
            @PathVariable UUID variantId) {

        log.info("Getting inventory for product: {} and variant: {}", productId, variantId);

        Inventory inventory = inventoryService.getInventoryByProductAndVariant(productId, variantId);
        return ResponseEntity.ok(inventory);
    }

    @GetMapping("/product/{productId}/available")
    @Operation(summary = "Get available quantity", description = "Retrieves total available quantity for a product")
    public ResponseEntity<Integer> getAvailableQuantity(@PathVariable UUID productId) {
        log.info("Getting available quantity for product: {}", productId);

        Integer availableQuantity = inventoryService.getAvailableQuantity(productId);
        return ResponseEntity.ok(availableQuantity);
    }

    @GetMapping("/product/{productId}/location/{locationId}/available")
    @Operation(summary = "Get available quantity by location", description = "Retrieves available quantity for a product at specific location")
    public ResponseEntity<Integer> getAvailableQuantityByLocation(
            @PathVariable UUID productId,
            @PathVariable UUID locationId) {

        log.info("Getting available quantity for product: {} at location: {}", productId, locationId);

        Integer availableQuantity = inventoryService.getAvailableQuantityByLocation(productId, locationId);
        return ResponseEntity.ok(availableQuantity);
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Get low stock inventory", description = "Retrieves inventory items with low stock")
    public ResponseEntity<List<Inventory>> getLowStockInventory(
            @RequestParam(defaultValue = "5") Integer threshold) {

        log.info("Getting low stock inventory with threshold: {}", threshold);

        List<Inventory> inventory = inventoryService.getLowStockInventory(threshold);
        return ResponseEntity.ok(inventory);
    }

    @GetMapping("/out-of-stock")
    @Operation(summary = "Get out of stock inventory", description = "Retrieves inventory items that are out of stock")
    public ResponseEntity<List<Inventory>> getOutOfStockInventory() {
        log.info("Getting out of stock inventory");

        List<Inventory> inventory = inventoryService.getOutOfStockInventory();
        return ResponseEntity.ok(inventory);
    }

    @GetMapping("/needing-restock")
    @Operation(summary = "Get inventory needing restock", description = "Retrieves inventory items that need restocking")
    public ResponseEntity<List<Inventory>> getInventoryNeedingRestock() {
        log.info("Getting inventory needing restock");

        List<Inventory> inventory = inventoryService.getInventoryNeedingRestock();
        return ResponseEntity.ok(inventory);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SELLER')")
    @Operation(summary = "Update inventory", description = "Updates inventory quantity")
    public ResponseEntity<Inventory> updateInventory(
            @PathVariable UUID id,
            @RequestParam Integer quantity) {

        log.info("Updating inventory: {} to quantity: {}", id, quantity);

        Inventory inventory = inventoryService.updateInventory(id, quantity);
        return ResponseEntity.ok(inventory);
    }

    @PostMapping("/reserve")
    @PreAuthorize("hasRole('USER') or hasRole('SELLER') or hasRole('ADMIN')")
    @Operation(summary = "Reserve stock", description = "Reserves stock for a product")
    public ResponseEntity<Void> reserveStock(
            @RequestParam UUID productId,
            @RequestParam(required = false) UUID variantId,
            @RequestParam(required = false) UUID locationId,
            @RequestParam Integer quantity,
            Authentication authentication) {

        log.info("Reserving stock for product: {}, variant: {}, location: {}, quantity: {}", 
                productId, variantId, locationId, quantity);

        // Extract user ID from authentication
        String userId = extractUserIdFromAuth(authentication);

        inventoryService.reserveStock(productId, variantId, locationId, quantity, UUID.fromString(userId));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/release")
    @PreAuthorize("hasRole('USER') or hasRole('SELLER') or hasRole('ADMIN')")
    @Operation(summary = "Release stock", description = "Releases reserved stock")
    public ResponseEntity<Void> releaseStock(
            @RequestParam UUID productId,
            @RequestParam(required = false) UUID variantId,
            @RequestParam(required = false) UUID locationId,
            @RequestParam Integer quantity) {

        log.info("Releasing stock for product: {}, variant: {}, location: {}, quantity: {}", 
                productId, variantId, locationId, quantity);

        inventoryService.releaseStock(productId, variantId, locationId, quantity);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/reserved")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SELLER')")
    @Operation(summary = "Update reserved quantity", description = "Updates the reserved quantity for inventory")
    public ResponseEntity<Void> updateReservedQuantity(
            @PathVariable UUID id,
            @RequestParam Integer reservedQuantity) {

        log.info("Updating reserved quantity for inventory: {} to {}", id, reservedQuantity);

        inventoryService.updateReservedQuantity(id, reservedQuantity);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/restock")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SELLER')")
    @Operation(summary = "Restock inventory", description = "Restocks inventory with additional quantity")
    public ResponseEntity<Void> restockInventory(
            @PathVariable UUID id,
            @RequestParam Integer quantity,
            Authentication authentication) {

        log.info("Restocking inventory: {} with quantity: {}", id, quantity);

        // Extract user ID from authentication
        String userId = extractUserIdFromAuth(authentication);

        inventoryService.restockInventory(id, quantity, UUID.fromString(userId));
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/reorder-level")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SELLER')")
    @Operation(summary = "Update reorder level", description = "Updates the reorder level for inventory")
    public ResponseEntity<Void> updateReorderLevel(
            @PathVariable UUID id,
            @RequestParam Integer reorderLevel) {

        log.info("Updating reorder level for inventory: {} to {}", id, reorderLevel);

        inventoryService.updateReorderLevel(id, reorderLevel);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/max-level")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SELLER')")
    @Operation(summary = "Update max level", description = "Updates the maximum level for inventory")
    public ResponseEntity<Void> updateMaxLevel(
            @PathVariable UUID id,
            @RequestParam Integer maxLevel) {

        log.info("Updating max level for inventory: {} to {}", id, maxLevel);

        inventoryService.updateMaxLevel(id, maxLevel);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete inventory", description = "Deletes inventory record")
    public ResponseEntity<Void> deleteInventory(@PathVariable UUID id) {
        log.info("Deleting inventory: {}", id);

        inventoryService.deleteInventory(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/product/{productId}/check")
    @Operation(summary = "Check stock availability", description = "Checks if product has sufficient stock")
    public ResponseEntity<Boolean> checkStockAvailability(
            @PathVariable UUID productId,
            @RequestParam Integer quantity) {

        log.info("Checking stock availability for product: {} with quantity: {}", productId, quantity);

        boolean hasStock = inventoryService.hasAvailableStock(productId, quantity);
        return ResponseEntity.ok(hasStock);
    }

    @GetMapping("/product/{productId}/location/{locationId}/check")
    @Operation(summary = "Check stock availability by location", description = "Checks if product has sufficient stock at location")
    public ResponseEntity<Boolean> checkStockAvailabilityByLocation(
            @PathVariable UUID productId,
            @PathVariable UUID locationId,
            @RequestParam Integer quantity) {

        log.info("Checking stock availability for product: {} at location: {} with quantity: {}", 
                productId, locationId, quantity);

        boolean hasStock = inventoryService.hasAvailableStockByLocation(productId, locationId, quantity);
        return ResponseEntity.ok(hasStock);
    }

    @GetMapping("/product/{productId}/total")
    @Operation(summary = "Get total quantity", description = "Retrieves total quantity for a product")
    public ResponseEntity<Integer> getTotalQuantity(@PathVariable UUID productId) {
        log.info("Getting total quantity for product: {}", productId);

        Integer totalQuantity = inventoryService.getTotalQuantity(productId);
        return ResponseEntity.ok(totalQuantity);
    }

    @GetMapping("/product/{productId}/reserved")
    @Operation(summary = "Get total reserved quantity", description = "Retrieves total reserved quantity for a product")
    public ResponseEntity<Integer> getTotalReservedQuantity(@PathVariable UUID productId) {
        log.info("Getting total reserved quantity for product: {}", productId);

        Integer totalReserved = inventoryService.getTotalReservedQuantity(productId);
        return ResponseEntity.ok(totalReserved);
    }

    @PostMapping("/product/{productId}/sync")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Sync inventory with product", description = "Synchronizes inventory data with product data")
    public ResponseEntity<Void> syncInventoryWithProduct(@PathVariable UUID productId) {
        log.info("Syncing inventory with product: {}", productId);

        inventoryService.syncInventoryWithProduct(productId);
        return ResponseEntity.ok().build();
    }

    private String extractUserIdFromAuth(Authentication authentication) {
        // Extract user ID from authentication
        // This is a simplified implementation
        return authentication.getName(); // Assuming username is the user ID
    }
}
