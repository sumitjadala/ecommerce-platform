package com.sj.product_service.repository;

import com.sj.product_service.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, UUID> {

    List<ProductCategory> findByProductId(UUID productId);

    List<ProductCategory> findByCategoryId(UUID categoryId);

    Optional<ProductCategory> findByProductIdAndCategoryId(UUID productId, UUID categoryId);

    Optional<ProductCategory> findByProductIdAndIsPrimaryTrue(UUID productId);

    @Query("SELECT pc FROM ProductCategory pc WHERE pc.product.id = :productId AND pc.isPrimary = true")
    Optional<ProductCategory> findPrimaryCategoryByProductId(@Param("productId") UUID productId);

    @Query("SELECT COUNT(pc) FROM ProductCategory pc WHERE pc.category.id = :categoryId")
    Long countByCategoryId(@Param("categoryId") UUID categoryId);

    @Query("SELECT COUNT(pc) FROM ProductCategory pc WHERE pc.product.id = :productId")
    Long countByProductId(@Param("productId") UUID productId);

    void deleteByProductId(UUID productId);

    void deleteByCategoryId(UUID categoryId);
}
