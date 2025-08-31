package com.sj.product_service.service.impl;

import com.sj.product_service.entity.Inventory;
import com.sj.product_service.entity.Product;
import com.sj.product_service.repository.InventoryRepository;
import com.sj.product_service.repository.ProductRepository;
import com.sj.product_service.service.InventoryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;

    @Override
    public Inventory getInventoryByProductId(UUID productId) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new RuntimeException("Product not found"));
        return inventoryRepository.findByProduct(product).orElseGet(() -> createInventoryForProduct(product));
    }

    @Transactional
    public Inventory createInventoryForProduct(Product product) {
        Inventory inventory = Inventory.builder()
                .product(product)
                .totalQuantity(0)
                .reservedQuantity(0)
                .reorderLevel(10)
                .lowStockAlert(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return inventoryRepository.save(inventory);
    }

    @Transactional
    public Inventory createInventoryForProductWithStock(Product product, int initialStock, int reorderLevel) {
        Inventory inventory = Inventory.builder()
                .product(product)
                .totalQuantity(initialStock)
                .reservedQuantity(initialStock)
                .reorderLevel(reorderLevel)
                .lowStockAlert(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return inventoryRepository.save(inventory);
    }

    @Override
    @Transactional
    public Inventory adjustStock(UUID productId, int quantityDelta) {
        Inventory inventory = getInventoryByProductId(productId);
        int newTotal = inventory.getTotalQuantity() + quantityDelta;
        if (newTotal < 0) throw new RuntimeException("Stock cannot be negative");
        inventory.setTotalQuantity(newTotal);
        inventory.setLowStockAlert(newTotal <= inventory.getReorderLevel());
        inventory.setUpdatedAt(LocalDateTime.now());
        return inventoryRepository.save(inventory);
    }

    @Override
    @Transactional
    public Inventory reserveStock(UUID productId, int quantity) {
        Inventory inventory = getInventoryByProductId(productId);
        int available = inventory.getAvailableQuantity();
        if (quantity > available) throw new RuntimeException("Insufficient stock");
        inventory.setReservedQuantity(inventory.getReservedQuantity() + quantity);
        inventory.setUpdatedAt(LocalDateTime.now());
        return inventoryRepository.save(inventory);
    }

    @Override
    @Transactional
    public Inventory releaseReservedStock(UUID productId, int quantity) {
        Inventory inventory = getInventoryByProductId(productId);
        int newReserved = inventory.getReservedQuantity() - quantity;
        if (newReserved < 0) newReserved = 0;
        inventory.setReservedQuantity(newReserved);
        inventory.setUpdatedAt(LocalDateTime.now());
        return inventoryRepository.save(inventory);
    }

    @Override
    @Transactional
    public Inventory fulfillOrder(UUID productId, int quantity) {
        Inventory inventory = getInventoryByProductId(productId);
        int available = inventory.getAvailableQuantity();
        if (quantity > available) throw new RuntimeException("Insufficient stock to fulfill");
        inventory.setTotalQuantity(inventory.getTotalQuantity() - quantity);
        inventory.setReservedQuantity(inventory.getReservedQuantity() - quantity);
        inventory.setLowStockAlert(inventory.getTotalQuantity() <= inventory.getReorderLevel());
        inventory.setUpdatedAt(LocalDateTime.now());
        return inventoryRepository.save(inventory);
    }

}
