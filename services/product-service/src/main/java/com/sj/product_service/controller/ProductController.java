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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Product Management", description = "APIs for managing products")
public class ProductController {

    private final ProductService productService;

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

//    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<Product> createProduct (
//            @RequestPart("product") String productId,
//            @RequestPart("image") MultipartFile imageFile) throws IOException {
//
//        Product savedProduct = productService.saveProductWithImage(productId, imageFile);
//        return ResponseEntity.ok(savedProduct);
//    }

    @PutMapping(path="/{productId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<Product> upload(
            @PathVariable String productId,
            @RequestPart("image") MultipartFile image) throws IOException {

        Product saved = productService.saveProductWithImage(productId, image);
        return ResponseEntity.created(URI.create("/products/" + saved.getId() + "/image")).body(saved);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Retrieves a specific product by its ID")
    public ResponseEntity<ProductResponseDto> getProduct(@PathVariable UUID id) {
        log.info("Getting product by ID: {}", id);

        ProductResponseDto product = productService.getProductById(id);
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


    private String extractSellerIdFromAuth(Authentication authentication) {
        if (authentication != null && authentication.getDetails() instanceof Map<?, ?> details) {
            Object sellerIdObj = details.get("sellerId");
            if (sellerIdObj != null) {
                return sellerIdObj.toString();
            }
        }
        throw new IllegalArgumentException("Seller ID not found in JWT claims");
    }
}
