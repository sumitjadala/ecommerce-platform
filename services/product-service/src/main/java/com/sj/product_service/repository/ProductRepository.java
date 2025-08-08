package com.sj.product_service.repository;

import com.sj.product_service.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    Optional<Product> findBySku(String sku);

    Optional<Product> findByName(String name);

    Optional<Product> findBySlug(String slug);

    List<Product> findBySellerId(UUID sellerId);

    Page<Product> findBySellerId(UUID sellerId, Pageable pageable);

    List<Product> findByStatus(Product.ProductStatus status);

    Page<Product> findByStatus(Product.ProductStatus status, Pageable pageable);

    List<Product> findByFeaturedTrue();

    List<Product> findByDigitalProductTrue();

    @Query("SELECT p FROM Product p WHERE p.status = 'ACTIVE'")
    List<Product> findActiveProducts();

    @Query("SELECT p FROM Product p WHERE p.status = 'ACTIVE'")
    Page<Product> findActiveProducts(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE " +
           "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:featured IS NULL OR p.featured = :featured) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice)")
    Page<Product> findProductsByFilters(
            @Param("name") String name,
            @Param("status") Product.ProductStatus status,
            @Param("featured") Boolean featured,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );

    @Query("SELECT p FROM Product p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.sku) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Product> searchProducts(@Param("searchTerm") String searchTerm, Pageable pageable);



    @Query("SELECT COUNT(p) FROM Product p WHERE p.sellerId = :sellerId")
    Long countBySellerId(@Param("sellerId") UUID sellerId);

    boolean existsBySku(String sku);

    boolean existsByName(String name);

    boolean existsBySlug(String slug);
}
