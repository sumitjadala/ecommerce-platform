package com.sj.product_service.service;

import com.sj.product_service.dto.ProductRequestDto;
import com.sj.product_service.dto.ProductResponseDto;
import com.sj.product_service.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface ProductService {

    ProductResponseDto createProduct(ProductRequestDto productRequestDto);

    ProductResponseDto updateProduct(UUID id, ProductRequestDto productRequestDto);

    ProductResponseDto getProductById(UUID id);

    ProductResponseDto getProductBySku(String sku);

    Page<ProductResponseDto> getAllProducts(Pageable pageable);

    Page<ProductResponseDto> getProductsByCategory(UUID categoryId, Pageable pageable);

    Page<ProductResponseDto> searchProducts(String searchTerm, Pageable pageable);

    Page<ProductResponseDto> getProductsByFilters(String name, UUID categoryId, BigDecimal minPrice, BigDecimal maxPrice, Product.ProductStatus status, Boolean isFeatured, Pageable pageable);

    List<ProductResponseDto> getFeaturedProducts();

    List<ProductResponseDto> getBestsellerProducts();

    List<ProductResponseDto> getAvailableProducts();

    Page<ProductResponseDto> getProductsBySeller(UUID sellerId, Pageable pageable);

    void deleteProduct(UUID id);

    void updateStockQuantity(UUID productId, Integer quantity);

    void updateProductStatus(UUID productId, Product.ProductStatus status);

    void updateProductRating(UUID productId, Double rating, Integer reviewCount);

    List<ProductResponseDto> getLowStockProducts(Integer threshold);

    Long getProductCountByCategory(UUID categoryId);

    boolean existsBySku(String sku);

    boolean existsByName(String name);

    boolean existsBySlug(String slug);
}
