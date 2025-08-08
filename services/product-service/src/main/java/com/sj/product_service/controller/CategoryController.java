package com.sj.product_service.controller;

import com.sj.product_service.dto.CategoryRequestDto;
import com.sj.product_service.dto.CategoryResponseDto;
import com.sj.product_service.service.CategoryService;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Category Management", description = "APIs for managing product categories")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new category", description = "Creates a new category with admin authentication")
    public ResponseEntity<CategoryResponseDto> createCategory(
            @Valid @RequestBody CategoryRequestDto categoryRequestDto) {

        log.info("Creating category: {}", categoryRequestDto.getName());

        CategoryResponseDto createdCategory = categoryService.createCategory(categoryRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
    }

    @GetMapping
    @Operation(summary = "Get all categories", description = "Retrieves all categories with pagination")
    public ResponseEntity<Page<CategoryResponseDto>> getCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {

        log.info("Getting categories with page: {}, size: {}", page, size);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<CategoryResponseDto> categories = categoryService.getAllCategories(pageable);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID", description = "Retrieves a specific category by its ID")
    public ResponseEntity<CategoryResponseDto> getCategory(@PathVariable UUID id) {
        log.info("Getting category by ID: {}", id);

        CategoryResponseDto category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get category by slug", description = "Retrieves a specific category by its slug")
    public ResponseEntity<CategoryResponseDto> getCategoryBySlug(@PathVariable String slug) {
        log.info("Getting category by slug: {}", slug);

        CategoryResponseDto category = categoryService.getCategoryBySlug(slug);
        return ResponseEntity.ok(category);
    }

    @GetMapping("/{id}/with-children")
    @Operation(summary = "Get category with children", description = "Retrieves a category with its sub-categories")
    public ResponseEntity<CategoryResponseDto> getCategoryWithChildren(@PathVariable UUID id) {
        log.info("Getting category with children: {}", id);

        CategoryResponseDto category = categoryService.getCategoryWithChildren(id);
        return ResponseEntity.ok(category);
    }

    @GetMapping("/root")
    @Operation(summary = "Get root categories", description = "Retrieves all root categories")
    public ResponseEntity<List<CategoryResponseDto>> getRootCategories() {
        log.info("Getting root categories");

        List<CategoryResponseDto> categories = categoryService.getRootCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{parentId}/children")
    @Operation(summary = "Get sub-categories", description = "Retrieves all sub-categories of a parent category")
    public ResponseEntity<List<CategoryResponseDto>> getSubCategories(@PathVariable UUID parentId) {
        log.info("Getting sub-categories for parent: {}", parentId);

        List<CategoryResponseDto> categories = categoryService.getSubCategories(parentId);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/tree")
    @Operation(summary = "Get category tree", description = "Retrieves the complete category hierarchy tree")
    public ResponseEntity<List<CategoryResponseDto>> getCategoryTree() {
        log.info("Getting category tree");

        List<CategoryResponseDto> categories = categoryService.getCategoryTree();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}/path")
    @Operation(summary = "Get category path", description = "Retrieves the path from root to the specified category")
    public ResponseEntity<List<CategoryResponseDto>> getCategoryPath(@PathVariable UUID id) {
        log.info("Getting category path for: {}", id);

        List<CategoryResponseDto> path = categoryService.getCategoryPath(id);
        return ResponseEntity.ok(path);
    }

    @GetMapping("/{id}/descendants")
    @Operation(summary = "Get category descendants", description = "Retrieves all descendants of a category")
    public ResponseEntity<List<CategoryResponseDto>> getCategoryDescendants(@PathVariable UUID id) {
        log.info("Getting category descendants for: {}", id);

        List<CategoryResponseDto> descendants = categoryService.getCategoryDescendants(id);
        return ResponseEntity.ok(descendants);
    }

    @GetMapping("/{id}/ancestors")
    @Operation(summary = "Get category ancestors", description = "Retrieves all ancestors of a category")
    public ResponseEntity<List<CategoryResponseDto>> getCategoryAncestors(@PathVariable UUID id) {
        log.info("Getting category ancestors for: {}", id);

        List<CategoryResponseDto> ancestors = categoryService.getCategoryAncestors(id);
        return ResponseEntity.ok(ancestors);
    }

    @GetMapping("/search")
    @Operation(summary = "Search categories", description = "Searches categories by name or description")
    public ResponseEntity<Page<CategoryResponseDto>> searchCategories(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("Searching categories with query: {}", q);

        Pageable pageable = PageRequest.of(page, size);
        Page<CategoryResponseDto> categories = categoryService.searchCategories(q, pageable);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/active")
    @Operation(summary = "Get active categories", description = "Retrieves all active categories")
    public ResponseEntity<List<CategoryResponseDto>> getActiveCategories() {
        log.info("Getting active categories");

        List<CategoryResponseDto> categories = categoryService.getActiveCategories();
        return ResponseEntity.ok(categories);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update category", description = "Updates an existing category")
    public ResponseEntity<CategoryResponseDto> updateCategory(
            @PathVariable UUID id,
            @Valid @RequestBody CategoryRequestDto categoryRequestDto) {

        log.info("Updating category: {}", id);

        CategoryResponseDto updatedCategory = categoryService.updateCategory(id, categoryRequestDto);
        return ResponseEntity.ok(updatedCategory);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete category", description = "Deletes a category (only if it has no products)")
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID id) {
        log.info("Deleting category: {}", id);

        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/move")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Move category", description = "Moves a category to a new parent")
    public ResponseEntity<Void> moveCategory(
            @PathVariable UUID id,
            @RequestParam(required = false) UUID newParentId) {

        log.info("Moving category: {} to parent: {}", id, newParentId);

        categoryService.moveCategory(id, newParentId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update category status", description = "Updates the active status of a category")
    public ResponseEntity<Void> updateCategoryStatus(
            @PathVariable UUID id,
            @RequestParam Boolean isActive) {

        log.info("Updating category status: {} to {}", id, isActive);

        categoryService.updateCategoryStatus(id, isActive);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/order")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update category order", description = "Updates the sort order of a category")
    public ResponseEntity<Void> updateCategoryOrder(
            @PathVariable UUID id,
            @RequestParam Integer sortOrder) {

        log.info("Updating category order: {} to {}", id, sortOrder);

        categoryService.updateCategoryOrder(id, sortOrder);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/check/name/{name}")
    @Operation(summary = "Check category name availability", description = "Checks if a category name is available")
    public ResponseEntity<Boolean> checkNameAvailability(@PathVariable String name) {
        log.info("Checking category name availability: {}", name);

        boolean exists = categoryService.existsByName(name);
        return ResponseEntity.ok(!exists); // true if available
    }

    @GetMapping("/check/slug/{slug}")
    @Operation(summary = "Check category slug availability", description = "Checks if a category slug is available")
    public ResponseEntity<Boolean> checkSlugAvailability(@PathVariable String slug) {
        log.info("Checking category slug availability: {}", slug);

        boolean exists = categoryService.existsBySlug(slug);
        return ResponseEntity.ok(!exists); // true if available
    }

    @GetMapping("/{id}/product-count")
    @Operation(summary = "Get category product count", description = "Retrieves the number of products in a category")
    public ResponseEntity<Long> getCategoryProductCount(@PathVariable UUID id) {
        log.info("Getting product count for category: {}", id);

        Long count = categoryService.getProductCountByCategory(id);
        return ResponseEntity.ok(count);
    }
}
