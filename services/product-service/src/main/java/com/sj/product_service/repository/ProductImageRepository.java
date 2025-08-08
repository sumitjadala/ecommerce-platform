package com.sj.product_service.repository;

import com.sj.product_service.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, UUID> {

    List<ProductImage> findByProductId(UUID productId);

    List<ProductImage> findByProductIdOrderBySortOrderAsc(UUID productId);

    List<ProductImage> findByVariantId(UUID variantId);

    Optional<ProductImage> findByProductIdAndIsPrimaryTrue(UUID productId);

    List<ProductImage> findByProductIdAndImageType(UUID productId, String imageType);

    @Query("SELECT pi FROM ProductImage pi WHERE pi.product.id = :productId AND pi.isPrimary = true")
    Optional<ProductImage> findPrimaryImageByProductId(@Param("productId") UUID productId);

    @Query("SELECT pi FROM ProductImage pi WHERE pi.product.id = :productId ORDER BY pi.sortOrder ASC")
    List<ProductImage> findImagesByProductIdOrdered(@Param("productId") UUID productId);

    @Query("SELECT pi FROM ProductImage pi WHERE pi.product.id = :productId AND pi.imageType = :imageType ORDER BY pi.sortOrder ASC")
    List<ProductImage> findImagesByProductIdAndType(@Param("productId") UUID productId, @Param("imageType") String imageType);

    @Query("SELECT COUNT(pi) FROM ProductImage pi WHERE pi.product.id = :productId")
    Long countByProductId(@Param("productId") UUID productId);

    void deleteByProductId(UUID productId);

    void deleteByVariantId(UUID variantId);
}
