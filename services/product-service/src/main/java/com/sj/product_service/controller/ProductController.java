package com.sj.product_service.controller;

import com.sj.product_service.dto.ProductRequestDto;
import com.sj.product_service.dto.ProductResponseDto;
import com.sj.product_service.entity.Product;
import com.sj.product_service.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Product Management", description = "APIs for managing products")
public class ProductController {

    private final ProductService productService;

    @GetMapping("/test")
    public ResponseEntity<String> testApi() {
        return ResponseEntity.ok("Api successful");
    }

    @PostMapping
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    @Operation(summary = "Create a new product", description = "Creates a new product with seller authentication")
    public ResponseEntity<ProductResponseDto> createProduct(
            @Valid @RequestBody ProductRequestDto productRequestDto,
            Authentication authentication) {

        log.info("Creating product: {}", productRequestDto.getName());
        String sellerId = extractSellerIdFromAuth(authentication);
        productRequestDto.setSellerId(UUID.fromString(sellerId));
        ProductResponseDto createdProduct = productService.createProduct(productRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    @GetMapping
    @Operation(summary = "Get all products", description = "Retrieves all products with pagination and filtering")
    public ResponseEntity<Page<ProductResponseDto>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean featured,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            Authentication authentication) {

        log.info("Getting products with page: {}, size: {}, search: {}", page, size, search);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ProductResponseDto> products;

        if (search != null && !search.trim().isEmpty()) {
            products = productService.searchProducts(search, pageable);
        } else {
            Product.ProductStatus productStatus = status != null ? Product.ProductStatus.valueOf(status.toUpperCase()) : null;
            products = productService.getProductsByFilters(null, null, minPrice, maxPrice, productStatus, featured, pageable);
        }

        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Retrieves a specific product by its ID")
    public ResponseEntity<ProductResponseDto> getProduct(@PathVariable UUID id) {
        log.info("Getting product by ID: {}", id);

        ProductResponseDto product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/sku/{sku}")
    @Operation(summary = "Get product by SKU", description = "Retrieves a specific product by its SKU")
    public ResponseEntity<ProductResponseDto> getProductBySku(@PathVariable String sku) {
        log.info("Getting product by SKU: {}", sku);

        ProductResponseDto product = productService.getProductBySku(sku);
        return ResponseEntity.ok(product);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@productOwnershipValidator.isOwnerOrAdmin(#id, authentication)")
    @Operation(summary = "Update product", description = "Updates an existing product")
    public ResponseEntity<ProductResponseDto> updateProduct(
            @PathVariable UUID id,
            @Valid @RequestBody ProductRequestDto productRequestDto,
            Authentication authentication) {

        log.info("Updating product: {}", id);

        // Extract seller ID from authentication
        String sellerId = extractSellerIdFromAuth(authentication);
        productRequestDto.setSellerId(UUID.fromString(sellerId));

        ProductResponseDto updatedProduct = productService.updateProduct(id, productRequestDto);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@productOwnershipValidator.isOwnerOrAdmin(#id, authentication)")
    @Operation(summary = "Delete product", description = "Soft deletes a product")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        log.info("Deleting product: {}", id);

        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/featured")
    @Operation(summary = "Get featured products", description = "Retrieves all featured products")
    public ResponseEntity<List<ProductResponseDto>> getFeaturedProducts() {
        log.info("Getting featured products");

        List<ProductResponseDto> products = productService.getFeaturedProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/available")
    @Operation(summary = "Get available products", description = "Retrieves all active products")
    public ResponseEntity<List<ProductResponseDto>> getAvailableProducts() {
        log.info("Getting available products");

        List<ProductResponseDto> products = productService.getAvailableProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/seller/{sellerId}")
    @PreAuthorize("hasRole('ADMIN') or #sellerId == authentication.principal.sellerId")
    @Operation(summary = "Get products by seller", description = "Retrieves all products for a specific seller")
    public ResponseEntity<Page<ProductResponseDto>> getProductsBySeller(
            @PathVariable UUID sellerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("Getting products by seller: {}", sellerId);

        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponseDto> products = productService.getProductsBySeller(sellerId, pageable);

        return ResponseEntity.ok(products);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("@productOwnershipValidator.isOwnerOrAdmin(#id, authentication)")
    @Operation(summary = "Update product status", description = "Updates the status of a product")
    public ResponseEntity<Void> updateProductStatus(
            @PathVariable UUID id,
            @RequestParam Product.ProductStatus status) {

        log.info("Updating product status: {} to {}", id, status);

        productService.updateProductStatus(id, status);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search products", description = "Searches products by various criteria")
    public ResponseEntity<Page<ProductResponseDto>> searchProducts(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("Searching products with query: {}", q);

        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponseDto> products = productService.searchProducts(q, pageable);

        return ResponseEntity.ok(products);
    }

    @GetMapping("/check/sku/{sku}")
    @Operation(summary = "Check SKU availability", description = "Checks if a SKU is available")
    public ResponseEntity<Boolean> checkSkuAvailability(@PathVariable String sku) {
        log.info("Checking SKU availability: {}", sku);

        boolean exists = productService.existsBySku(sku);
        return ResponseEntity.ok(!exists); // true if available
    }

    @GetMapping("/check/slug/{slug}")
    @Operation(summary = "Check slug availability", description = "Checks if a slug is available")
    public ResponseEntity<Boolean> checkSlugAvailability(@PathVariable String slug) {
        log.info("Checking slug availability: {}", slug);

        boolean exists = productService.existsBySlug(slug);
        return ResponseEntity.ok(!exists); // true if available
    }

    private String extractSellerIdFromAuth(Authentication authentication) {
        // TODO: Extract from JWT claims
        return "00000000-0000-0000-0000-000000000001";
    }
}
