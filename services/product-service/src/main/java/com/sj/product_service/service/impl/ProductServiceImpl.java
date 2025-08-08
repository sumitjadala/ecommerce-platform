package com.sj.product_service.service.impl;

import com.sj.product_service.dto.ProductRequestDto;
import com.sj.product_service.dto.ProductResponseDto;
import com.sj.product_service.entity.Product;
import com.sj.product_service.events.ProductEvents;
import com.sj.product_service.repository.ProductRepository;
import com.sj.product_service.service.CacheService;
import com.sj.product_service.service.EventPublisher;
import com.sj.product_service.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.sj.product_service.cache.CacheKeys.product;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CacheService cacheService;
    private final EventPublisher eventPublisher;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public ProductResponseDto createProduct(ProductRequestDto productRequestDto) {
        log.info("Creating product: {}", productRequestDto.getName());

        if (productRepository.existsBySku(productRequestDto.getSku())) {
            throw new IllegalArgumentException("SKU already exists: " + productRequestDto.getSku());
        }
        if (productRepository.existsBySlug(productRequestDto.getSlug())) {
            throw new IllegalArgumentException("Slug already exists: " + productRequestDto.getSlug());
        }

        Product product = Product.builder()
                .sellerId(productRequestDto.getSellerId())
                .sku(productRequestDto.getSku())
                .name(productRequestDto.getName())
                .slug(productRequestDto.getSlug())
                .description(productRequestDto.getDescription())
                .shortDescription(productRequestDto.getShortDescription())
                .price(productRequestDto.getPrice())
                .costPrice(productRequestDto.getCostPrice())
                .currency(productRequestDto.getCurrency())
                .status(productRequestDto.getStatus())
                .weight(productRequestDto.getWeight())
                .dimensions(productRequestDto.getDimensions())
                .tags(productRequestDto.getTags())
                .seoTitle(productRequestDto.getSeoTitle())
                .seoDescription(productRequestDto.getSeoDescription())
                .metaKeywords(productRequestDto.getMetaKeywords())
                .featured(productRequestDto.getFeatured())
                .digitalProduct(productRequestDto.getDigitalProduct())
                .build();

        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with ID: {}", savedProduct.getId());

        // Publish lifecycle events (Kafka + local)
        eventPublisher.publishProductCreated(savedProduct);
        ProductEvents.ProductLifecycleEvent localEvent = ProductEvents.ProductLifecycleEvent.builder()
                .eventType("CREATED")
                .productId(savedProduct.getId())
                .sellerId(savedProduct.getSellerId())
                .timestamp(Instant.now())
                .version(1)
                .build();
        applicationEventPublisher.publishEvent(localEvent);

        // Prime cache
        cacheService.set(product(savedProduct.getId().toString()), ProductResponseDto.fromEntity(savedProduct), Duration.ofMinutes(15));

        return ProductResponseDto.fromEntity(savedProduct);
    }

    @Override
    public ProductResponseDto updateProduct(UUID id, ProductRequestDto productRequestDto) {
        log.info("Updating product: {}", id);

        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
        Product before = Product.builder()
                .id(existingProduct.getId())
                .sellerId(existingProduct.getSellerId())
                .sku(existingProduct.getSku())
                .name(existingProduct.getName())
                .slug(existingProduct.getSlug())
                .description(existingProduct.getDescription())
                .shortDescription(existingProduct.getShortDescription())
                .price(existingProduct.getPrice())
                .costPrice(existingProduct.getCostPrice())
                .currency(existingProduct.getCurrency())
                .status(existingProduct.getStatus())
                .weight(existingProduct.getWeight())
                .dimensions(existingProduct.getDimensions())
                .tags(existingProduct.getTags())
                .seoTitle(existingProduct.getSeoTitle())
                .seoDescription(existingProduct.getSeoDescription())
                .metaKeywords(existingProduct.getMetaKeywords())
                .featured(existingProduct.getFeatured())
                .digitalProduct(existingProduct.getDigitalProduct())
                .createdAt(existingProduct.getCreatedAt())
                .updatedAt(existingProduct.getUpdatedAt())
                .createdBy(existingProduct.getCreatedBy())
                .updatedBy(existingProduct.getUpdatedBy())
                .build();

        if (!existingProduct.getSku().equals(productRequestDto.getSku()) &&
            productRepository.existsBySku(productRequestDto.getSku())) {
            throw new IllegalArgumentException("SKU already exists: " + productRequestDto.getSku());
        }
        if (!existingProduct.getSlug().equals(productRequestDto.getSlug()) &&
            productRepository.existsBySlug(productRequestDto.getSlug())) {
            throw new IllegalArgumentException("Slug already exists: " + productRequestDto.getSlug());
        }

        existingProduct.setSku(productRequestDto.getSku());
        existingProduct.setName(productRequestDto.getName());
        existingProduct.setSlug(productRequestDto.getSlug());
        existingProduct.setDescription(productRequestDto.getDescription());
        existingProduct.setShortDescription(productRequestDto.getShortDescription());
        existingProduct.setPrice(productRequestDto.getPrice());
        existingProduct.setCostPrice(productRequestDto.getCostPrice());
        existingProduct.setCurrency(productRequestDto.getCurrency());
        existingProduct.setStatus(productRequestDto.getStatus());
        existingProduct.setWeight(productRequestDto.getWeight());
        existingProduct.setDimensions(productRequestDto.getDimensions());
        existingProduct.setTags(productRequestDto.getTags());
        existingProduct.setSeoTitle(productRequestDto.getSeoTitle());
        existingProduct.setSeoDescription(productRequestDto.getSeoDescription());
        existingProduct.setMetaKeywords(productRequestDto.getMetaKeywords());
        existingProduct.setFeatured(productRequestDto.getFeatured());
        existingProduct.setDigitalProduct(productRequestDto.getDigitalProduct());

        Product updatedProduct = productRepository.save(existingProduct);
        log.info("Product updated successfully: {}", id);

        // Publish lifecycle events
        eventPublisher.publishProductUpdated(before, updatedProduct);
        ProductEvents.ProductLifecycleEvent localEvent = ProductEvents.ProductLifecycleEvent.builder()
                .eventType("UPDATED")
                .productId(updatedProduct.getId())
                .sellerId(updatedProduct.getSellerId())
                .timestamp(Instant.now())
                .version(1)
                .build();
        applicationEventPublisher.publishEvent(localEvent);

        // Invalidate cache; it will be repopulated on next get
        cacheService.delete(product(updatedProduct.getId().toString()));

        return ProductResponseDto.fromEntity(updatedProduct);
    }

    @Override
    public ProductResponseDto getProductById(UUID id) {
        log.info("Getting product by ID: {}", id);

        String key = product(id.toString());
        ProductResponseDto cached = cacheService.get(key, ProductResponseDto.class);
        if (cached != null) {
            return cached;
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));

        ProductResponseDto dto = ProductResponseDto.fromEntity(product);
        cacheService.set(key, dto, Duration.ofMinutes(15));
        return dto;
    }

    @Override
    public ProductResponseDto getProductBySku(String sku) {
        log.info("Getting product by SKU: {}", sku);

        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with SKU: " + sku));

        return ProductResponseDto.fromEntity(product);
    }

    @Override
    public Page<ProductResponseDto> getAllProducts(Pageable pageable) {
        log.info("Getting all products with pagination");

        Page<Product> products = productRepository.findAll(pageable);
        return products.map(ProductResponseDto::fromEntity);
    }

    @Override
    public Page<ProductResponseDto> getProductsByCategory(UUID categoryId, Pageable pageable) {
        log.info("Getting products by category: {}", categoryId);
        return Page.empty(pageable);
    }

    @Override
    public Page<ProductResponseDto> searchProducts(String searchTerm, Pageable pageable) {
        log.info("Searching products with term: {}", searchTerm);

        Page<Product> products = productRepository.searchProducts(searchTerm, pageable);
        return products.map(ProductResponseDto::fromEntity);
    }

    @Override
    public Page<ProductResponseDto> getProductsByFilters(
            String name,
            UUID categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Product.ProductStatus status,
            Boolean isFeatured,
            Pageable pageable) {

        log.info("Filtering products with name: {}, status: {}, featured: {}", name, status, isFeatured);

        Page<Product> products = productRepository.findProductsByFilters(name, status, isFeatured, minPrice, maxPrice, pageable);
        return products.map(ProductResponseDto::fromEntity);
    }

    @Override
    public List<ProductResponseDto> getFeaturedProducts() {
        log.info("Getting featured products");

        List<Product> products = productRepository.findByFeaturedTrue();
        return products.stream()
                .map(ProductResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponseDto> getBestsellerProducts() {
        log.info("Getting bestseller products");
        return List.of();
    }

    @Override
    public List<ProductResponseDto> getAvailableProducts() {
        log.info("Getting available products");

        List<Product> products = productRepository.findActiveProducts();
        return products.stream()
                .map(ProductResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ProductResponseDto> getProductsBySeller(UUID sellerId, Pageable pageable) {
        log.info("Getting products by seller: {}", sellerId);

        Page<Product> products = productRepository.findBySellerId(sellerId, pageable);
        return products.map(ProductResponseDto::fromEntity);
    }

    @Override
    public void deleteProduct(UUID id) {
        log.info("Deleting product: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));

        product.setStatus(Product.ProductStatus.ARCHIVED);
        productRepository.save(product);

        // Publish lifecycle events
        eventPublisher.publishProductDeleted(product);
        ProductEvents.ProductLifecycleEvent localEvent = ProductEvents.ProductLifecycleEvent.builder()
                .eventType("DELETED")
                .productId(product.getId())
                .sellerId(product.getSellerId())
                .timestamp(Instant.now())
                .version(1)
                .build();
        applicationEventPublisher.publishEvent(localEvent);

        cacheService.delete(product(product.getId().toString()));
        log.info("Product deleted successfully: {}", id);
    }

    @Override
    public void updateStockQuantity(UUID productId, Integer quantity) {
        log.info("Updating stock quantity for product: {} to {}", productId, quantity);
        // Publish inventory event
        ProductEvents.InventoryEvent event = ProductEvents.InventoryEvent.builder()
                .eventType(quantity != null && quantity <= 5 ? "LOW_STOCK" : "STOCK_UPDATED")
                .productId(productId)
                .newQuantity(quantity)
                .timestamp(Instant.now())
                .build();
        eventPublisher.publishInventoryChanged(event);
        applicationEventPublisher.publishEvent(event);
    }

    @Override
    public void updateProductStatus(UUID productId, Product.ProductStatus status) {
        log.info("Updating product status: {} to {}", productId, status);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        product.setStatus(status);
        productRepository.save(product);

        eventPublisher.publishProductStatusChanged(productId, status.name());
        ProductEvents.ProductLifecycleEvent localEvent = ProductEvents.ProductLifecycleEvent.builder()
                .eventType("STATUS_CHANGED")
                .productId(productId)
                .sellerId(product.getSellerId())
                .timestamp(Instant.now())
                .version(1)
                .build();
        applicationEventPublisher.publishEvent(localEvent);

        cacheService.delete(product(productId.toString()));
        log.info("Product status updated successfully: {} to {}", productId, status);
    }

    @Override
    public void updateProductRating(UUID productId, Double rating, Integer reviewCount) {
        log.info("Updating product rating: {} to rating: {}, reviewCount: {}", productId, rating, reviewCount);
        // TODO: Implement metrics update and publish analytics event
    }

    @Override
    public List<ProductResponseDto> getLowStockProducts(Integer threshold) {
        log.info("Getting low stock products with threshold: {}", threshold);
        return List.of();
    }

    @Override
    public Long getProductCountByCategory(UUID categoryId) {
        log.info("Getting product count by category: {}", categoryId);
        return 0L;
    }

    @Override
    public boolean existsBySku(String sku) {
        return productRepository.existsBySku(sku);
    }

    @Override
    public boolean existsByName(String name) {
        return productRepository.existsByName(name);
    }

    @Override
    public boolean existsBySlug(String slug) {
        return productRepository.existsBySlug(slug);
    }
}
