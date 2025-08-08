package com.sj.product_service.controller;

import com.sj.product_service.dto.CategoryResponseDto;
import com.sj.product_service.dto.ProductResponseDto;
import com.sj.product_service.service.CategoryService;
import com.sj.product_service.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/public")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Public Catalog", description = "Public product browsing APIs")
public class PublicProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    @GetMapping("/products/{id}")
    @Cacheable(value = "publicProducts", key = "#id")
    @Operation(summary = "Get public product by ID")
    public ResponseEntity<ProductResponseDto> getPublicProduct(@PathVariable UUID id) {
        ProductResponseDto dto = productService.getProductById(id);
        if (dto == null || dto.getStatus() != com.sj.product_service.entity.Product.ProductStatus.ACTIVE) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/search")
    @Cacheable(value = "publicSearch", key = "#q + '_' + #page + '_' + #size")
    @Operation(summary = "Search public products")
    public ResponseEntity<Page<ProductResponseDto>> searchProducts(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponseDto> results = productService.searchProducts(q, pageable);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/categories")
    @Cacheable(value = "publicCategories", key = "'all'")
    @Operation(summary = "Get all public categories")
    public ResponseEntity<List<CategoryResponseDto>> getAllCategories() {
        List<CategoryResponseDto> categories = categoryService.getActiveCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/categories/tree")
    @Cacheable(value = "publicCategoryTree", key = "'tree'")
    @Operation(summary = "Get category tree")
    public ResponseEntity<List<CategoryResponseDto>> getCategoryTree() {
        List<CategoryResponseDto> categories = categoryService.getCategoryTree();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/categories/root")
    @Cacheable(value = "publicRootCategories", key = "'root'")
    @Operation(summary = "Get root categories")
    public ResponseEntity<List<CategoryResponseDto>> getRootCategories() {
        List<CategoryResponseDto> categories = categoryService.getRootCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/categories/{id}")
    @Cacheable(value = "publicCategory", key = "#id")
    @Operation(summary = "Get category by ID")
    public ResponseEntity<CategoryResponseDto> getCategory(@PathVariable UUID id) {
        CategoryResponseDto category = categoryService.getCategoryById(id);
        if (category == null || !category.getIsActive()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(category);
    }

    @GetMapping("/categories/slug/{slug}")
    @Cacheable(value = "publicCategoryBySlug", key = "#slug")
    @Operation(summary = "Get category by slug")
    public ResponseEntity<CategoryResponseDto> getCategoryBySlug(@PathVariable String slug) {
        CategoryResponseDto category = categoryService.getCategoryBySlug(slug);
        if (category == null || !category.getIsActive()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(category);
    }

    @GetMapping("/categories/{id}/children")
    @Cacheable(value = "publicCategoryChildren", key = "#id")
    @Operation(summary = "Get category children")
    public ResponseEntity<List<CategoryResponseDto>> getCategoryChildren(@PathVariable UUID id) {
        List<CategoryResponseDto> children = categoryService.getSubCategories(id);
        return ResponseEntity.ok(children);
    }

    @GetMapping("/categories/{id}/products")
    @Cacheable(value = "publicCategoryProducts", key = "#id + '_' + #page + '_' + #size")
    @Operation(summary = "Get products by category")
    public ResponseEntity<Page<ProductResponseDto>> getProductsByCategory(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponseDto> products = productService.getProductsByCategory(id, pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/categories/{id}/path")
    @Cacheable(value = "publicCategoryPath", key = "#id")
    @Operation(summary = "Get category path")
    public ResponseEntity<List<CategoryResponseDto>> getCategoryPath(@PathVariable UUID id) {
        List<CategoryResponseDto> path = categoryService.getCategoryPath(id);
        return ResponseEntity.ok(path);
    }

    @GetMapping("/featured")
    @Cacheable(value = "publicFeaturedProducts", key = "'featured'")
    @Operation(summary = "Get featured products")
    public ResponseEntity<List<ProductResponseDto>> getFeaturedProducts() {
        List<ProductResponseDto> products = productService.getFeaturedProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/bestsellers")
    @Cacheable(value = "publicBestsellers", key = "'bestsellers'")
    @Operation(summary = "Get bestseller products")
    public ResponseEntity<List<ProductResponseDto>> getBestsellerProducts() {
        List<ProductResponseDto> products = productService.getBestsellerProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/available")
    @Cacheable(value = "publicAvailableProducts", key = "'available'")
    @Operation(summary = "Get available products")
    public ResponseEntity<List<ProductResponseDto>> getAvailableProducts() {
        List<ProductResponseDto> products = productService.getAvailableProducts();
        return ResponseEntity.ok(products);
    }
}
