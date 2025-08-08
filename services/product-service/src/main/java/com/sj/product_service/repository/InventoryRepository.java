package com.sj.product_service.repository;

import com.sj.product_service.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, UUID> {

    List<Inventory> findByProductId(UUID productId);

    Optional<Inventory> findByProductIdAndVariantId(UUID productId, UUID variantId);

    Optional<Inventory> findByProductIdAndVariantIdAndLocationId(UUID productId, UUID variantId, UUID locationId);

    List<Inventory> findByProductIdAndLocationId(UUID productId, UUID locationId);

    @Query("SELECT i FROM Inventory i WHERE i.product.id = :productId AND i.availableQuantity > 0")
    List<Inventory> findAvailableInventoryByProductId(@Param("productId") UUID productId);

    @Query("SELECT i FROM Inventory i WHERE i.availableQuantity <= i.reorderLevel")
    List<Inventory> findLowStockInventory();

    @Query("SELECT i FROM Inventory i WHERE i.availableQuantity <= :threshold")
    List<Inventory> findLowStockInventoryByThreshold(@Param("threshold") Integer threshold);

    @Query("SELECT i FROM Inventory i WHERE i.product.id = :productId AND i.availableQuantity > 0")
    List<Inventory> findAvailableInventory(@Param("productId") UUID productId);

    @Query("SELECT SUM(i.availableQuantity) FROM Inventory i WHERE i.product.id = :productId")
    Integer getTotalAvailableQuantity(@Param("productId") UUID productId);

    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.product.id = :productId")
    Long countByProductId(@Param("productId") UUID productId);

    void deleteByProductId(UUID productId);

    void deleteByVariantId(UUID variantId);
}
