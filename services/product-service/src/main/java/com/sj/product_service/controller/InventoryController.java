package com.sj.product_service.controller;

import com.sj.product_service.dto.InventoryAdjustmentRequest;
import com.sj.product_service.entity.Inventory;
import com.sj.product_service.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/{productId}")
    public ResponseEntity<Inventory> getInventory(@PathVariable UUID productId) {
        Inventory inventory = inventoryService.getInventoryByProductId(productId);
        return ResponseEntity.ok(inventory);
    }

    @PostMapping("/{productId}/adjust")
    public ResponseEntity<Inventory> adjustStock(@PathVariable UUID productId,
                                                 @RequestBody InventoryAdjustmentRequest request) {
        Inventory inventory = inventoryService.adjustStock(productId, request.getQuantityDelta());
        return ResponseEntity.ok(inventory);
    }

    @PostMapping("/{productId}/reserve")
    public ResponseEntity<Inventory> reserveStock(@PathVariable UUID productId,
                                                  @RequestParam int quantity) {
        Inventory inventory = inventoryService.reserveStock(productId, quantity);
        return ResponseEntity.ok(inventory);
    }

    @PostMapping("/{productId}/release")
    public ResponseEntity<Inventory> releaseReservedStock(@PathVariable UUID productId,
                                                          @RequestParam int quantity) {
        Inventory inventory = inventoryService.releaseReservedStock(productId, quantity);
        return ResponseEntity.ok(inventory);
    }

    @PostMapping("/{productId}/fulfill")
    public ResponseEntity<Inventory> fulfillOrder(@PathVariable UUID productId,
                                                  @RequestParam int quantity) {
        Inventory inventory = inventoryService.fulfillOrder(productId, quantity);
        return ResponseEntity.ok(inventory);
    }

}
