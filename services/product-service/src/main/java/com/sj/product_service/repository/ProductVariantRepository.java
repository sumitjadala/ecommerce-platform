package com.sj.product_service.repository;

import com.sj.product_service.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, UUID> {

    List<ProductVariant> findByProductId(UUID productId);

    Optional<ProductVariant> findBySku(String sku);

    Optional<ProductVariant> findByProductIdAndIsDefaultTrue(UUID productId);

    List<ProductVariant> findByProductIdAndStatus(UUID productId, ProductVariant.ProductStatus status);

    @Query("SELECT pv FROM ProductVariant pv WHERE pv.product.id = :productId AND pv.status = 'ACTIVE'")
    List<ProductVariant> findActiveVariantsByProductId(@Param("productId") UUID productId);

    // Note: This query would need native SQL for JSONB search
    // @Query("SELECT pv FROM ProductVariant pv WHERE pv.attributes::text LIKE %:attributeValue%")
    // List<ProductVariant> findByAttributeValue(@Param("attributeValue") String attributeValue);

    @Query("SELECT COUNT(pv) FROM ProductVariant pv WHERE pv.product.id = :productId")
    Long countByProductId(@Param("productId") UUID productId);

    boolean existsBySku(String sku);

    void deleteByProductId(UUID productId);
}
