package com.sj.product_service.repository;

import com.sj.product_service.entity.Inventory;
import com.sj.product_service.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InventoryRepository extends JpaRepository<Inventory, UUID> {
    Optional<Inventory> findByProduct(Product product);
}
