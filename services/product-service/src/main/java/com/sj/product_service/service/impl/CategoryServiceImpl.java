package com.sj.product_service.service.impl;

import com.sj.product_service.dto.CategoryRequestDto;
import com.sj.product_service.dto.CategoryResponseDto;
import com.sj.product_service.entity.Category;
import com.sj.product_service.events.ProductEvents;
import com.sj.product_service.repository.CategoryRepository;
import com.sj.product_service.repository.ProductCategoryRepository;
import com.sj.product_service.service.CacheService;
import com.sj.product_service.service.CategoryService;
import com.sj.product_service.service.EventPublisher;
import com.sj.product_service.service.SlugService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final CacheService cacheService;
    private final EventPublisher eventPublisher;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final SlugService slugService;

    @Override
    public CategoryResponseDto createCategory(CategoryRequestDto categoryRequestDto) {
        log.info("Creating category: {}", categoryRequestDto.getName());

        if (categoryRepository.existsByName(categoryRequestDto.getName())) {
            throw new IllegalArgumentException("Category name already exists: " + categoryRequestDto.getName());
        }

        if (categoryRepository.existsBySlug(categoryRequestDto.getSlug())) {
            throw new IllegalArgumentException("Category slug already exists: " + categoryRequestDto.getSlug());
        }

        Category category = Category.builder()
                .name(categoryRequestDto.getName())
                .slug(categoryRequestDto.getSlug())
                .description(categoryRequestDto.getDescription())
                .seoTitle(categoryRequestDto.getSeoTitle())
                .seoDescription(categoryRequestDto.getSeoDescription())
                .imageUrl(categoryRequestDto.getImageUrl())
                .sortOrder(categoryRequestDto.getSortOrder())
                .isActive(categoryRequestDto.getIsActive())
                .build();

        // Handle parent category and nested set model
        if (categoryRequestDto.getParentId() != null) {
            Category parent = categoryRepository.findById(categoryRequestDto.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("Parent category not found"));
            category.setParent(parent);
            category.setLevel(parent.getLevel() + 1);
            
            // Calculate hierarchy path
            String parentPath = parent.getHierarchyPath() != null ? parent.getHierarchyPath() : "/" + parent.getSlug();
            category.setHierarchyPath(parentPath + "/" + category.getSlug());
            
            // Set nested set bounds (simplified - in production, you'd need more complex logic)
            category.setLeftBound(parent.getRightBound());
            category.setRightBound(parent.getRightBound() + 1);
            
            // Update parent's right bound
            parent.setRightBound(parent.getRightBound() + 2);
            categoryRepository.save(parent);
        } else {
            // Root category
            category.setLevel(0);
            category.setHierarchyPath("/" + category.getSlug());
            
            // Find max right bound for root categories
            Integer maxRightBound = categoryRepository.findByParentIsNull().stream()
                    .mapToInt(Category::getRightBound)
                    .max()
                    .orElse(0);
            category.setLeftBound(maxRightBound + 1);
            category.setRightBound(maxRightBound + 2);
        }

        Category savedCategory = categoryRepository.save(category);
        CategoryResponseDto response = CategoryResponseDto.fromEntity(savedCategory);

        // Publish events and cache invalidation
        publishCategoryCreated(savedCategory);
        invalidateCategoryCache(savedCategory);

        log.info("Category created successfully: {}", savedCategory.getId());
        return response;
    }

    @Override
    public CategoryResponseDto updateCategory(UUID id, CategoryRequestDto categoryRequestDto) {
        log.info("Updating category: {}", id);

        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + id));

        // Store before state for comparison (simplified)
        Category before = existingCategory;

        existingCategory.setName(categoryRequestDto.getName());
        existingCategory.setSlug(categoryRequestDto.getSlug());
        existingCategory.setDescription(categoryRequestDto.getDescription());
        existingCategory.setSeoTitle(categoryRequestDto.getSeoTitle());
        existingCategory.setSeoDescription(categoryRequestDto.getSeoDescription());
        existingCategory.setImageUrl(categoryRequestDto.getImageUrl());
        existingCategory.setSortOrder(categoryRequestDto.getSortOrder());
        existingCategory.setIsActive(categoryRequestDto.getIsActive());

        Category savedCategory = categoryRepository.save(existingCategory);
        CategoryResponseDto response = CategoryResponseDto.fromEntity(savedCategory);

        // Publish events and cache invalidation
        publishCategoryUpdated(before, savedCategory);
        invalidateCategoryCache(savedCategory);

        log.info("Category updated successfully: {}", id);
        return response;
    }

    @Override
    public CategoryResponseDto getCategoryById(UUID id) {
        log.info("Getting category by ID: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + id));

        return CategoryResponseDto.fromEntity(category);
    }

    @Override
    public CategoryResponseDto getCategoryBySlug(String slug) {
        log.info("Getting category by slug: {}", slug);

        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + slug));

        return CategoryResponseDto.fromEntity(category);
    }

    @Override
    public Page<CategoryResponseDto> getAllCategories(Pageable pageable) {
        log.info("Getting all categories with pagination");

        Page<Category> categories = categoryRepository.findAll(pageable);
        return categories.map(CategoryResponseDto::fromEntity);
    }

    @Override
    public List<CategoryResponseDto> getRootCategories() {
        log.info("Getting root categories");

        List<Category> categories = categoryRepository.findActiveRootCategories();
        return categories.stream()
                .map(CategoryResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryResponseDto> getSubCategories(UUID parentId) {
        log.info("Getting sub-categories for parent: {}", parentId);

        List<Category> categories = categoryRepository.findActiveSubCategories(parentId);
        return categories.stream()
                .map(CategoryResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryResponseDto> getCategoryTree() {
        log.info("Getting complete category tree");

        List<Category> rootCategories = categoryRepository.findActiveRootCategories();
        return rootCategories.stream()
                .map(this::buildCategoryTree)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryResponseDto> getCategoryPath(UUID categoryId) {
        log.info("Getting category path for: {}", categoryId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));

        List<Category> ancestors = categoryRepository.findAncestors(category.getLeftBound(), category.getRightBound());
        ancestors.add(category);

        return ancestors.stream()
                .map(CategoryResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryResponseDto> getCategoryDescendants(UUID categoryId) {
        log.info("Getting category descendants for: {}", categoryId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));

        List<Category> descendants = categoryRepository.findDescendants(category.getLeftBound(), category.getRightBound());
        return descendants.stream()
                .map(CategoryResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryResponseDto> getCategoryAncestors(UUID categoryId) {
        log.info("Getting category ancestors for: {}", categoryId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));

        List<Category> ancestors = categoryRepository.findAncestors(category.getLeftBound(), category.getRightBound());
        return ancestors.stream()
                .map(CategoryResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Page<CategoryResponseDto> searchCategories(String searchTerm, Pageable pageable) {
        log.info("Searching categories with term: {}", searchTerm);

        // This would need a custom implementation in repository
        // For now, returning empty page
        return Page.empty(pageable);
    }

    @Override
    public void deleteCategory(UUID id) {
        log.info("Deleting category: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + id));

        // Check if category has products
        Long productCount = productCategoryRepository.countByCategoryId(id);
        if (productCount > 0) {
            throw new IllegalStateException("Cannot delete category with products. Product count: " + productCount);
        }

        categoryRepository.delete(category);

        // Publish events and cache invalidation
        publishCategoryDeleted(category);
        invalidateCategoryCache(category);

        log.info("Category deleted successfully: {}", id);
    }

    @Override
    public void moveCategory(UUID categoryId, UUID newParentId) {
        log.info("Moving category: {} to parent: {}", categoryId, newParentId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));

        Category newParent = null;
        if (newParentId != null) {
            newParent = categoryRepository.findById(newParentId)
                    .orElseThrow(() -> new IllegalArgumentException("New parent category not found: " + newParentId));
        }

        // This is a simplified implementation
        // In production, you'd need complex nested set model operations
        category.setParent(newParent);
        categoryRepository.save(category);

        log.info("Category moved successfully: {} to parent: {}", categoryId, newParentId);
    }

    @Override
    public void updateCategoryStatus(UUID id, Boolean isActive) {
        log.info("Updating category status: {} to {}", id, isActive);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + id));

        category.setIsActive(isActive);
        categoryRepository.save(category);

        invalidateCategoryCache(category);
        log.info("Category status updated successfully: {} to {}", id, isActive);
    }

    @Override
    public void updateCategoryOrder(UUID id, Integer sortOrder) {
        log.info("Updating category order: {} to {}", id, sortOrder);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + id));

        category.setSortOrder(sortOrder);
        categoryRepository.save(category);

        invalidateCategoryCache(category);
        log.info("Category order updated successfully: {} to {}", id, sortOrder);
    }

    @Override
    public boolean existsByName(String name) {
        return categoryRepository.existsByName(name);
    }

    @Override
    public boolean existsBySlug(String slug) {
        return categoryRepository.existsBySlug(slug);
    }

    @Override
    public Long getProductCountByCategory(UUID categoryId) {
        return productCategoryRepository.countByCategoryId(categoryId);
    }

    @Override
    public List<CategoryResponseDto> getActiveCategories() {
        log.info("Getting active categories");

        List<Category> categories = categoryRepository.findByIsActiveTrueOrderBySortOrderAsc();
        return categories.stream()
                .map(CategoryResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryResponseDto getCategoryWithChildren(UUID id) {
        log.info("Getting category with children: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + id));

        CategoryResponseDto response = CategoryResponseDto.fromEntity(category);
        response.setChildren(getSubCategories(id));
        response.setProductCount(getProductCountByCategory(id));

        return response;
    }

    private CategoryResponseDto buildCategoryTree(Category category) {
        CategoryResponseDto dto = CategoryResponseDto.fromEntity(category);
        List<CategoryResponseDto> children = getSubCategories(category.getId());
        dto.setChildren(children);
        dto.setProductCount(getProductCountByCategory(category.getId()));
        return dto;
    }

    private void publishCategoryCreated(Category category) {
        // Publish to Kafka
        ProductEvents.ProductLifecycleEvent event = ProductEvents.ProductLifecycleEvent.builder()
                .eventType("CATEGORY_CREATED")
                .productId(null) // Not applicable for categories
                .timestamp(Instant.now())
                .version(1)
                .build();
        eventPublisher.publishProductAnalytics(ProductEvents.ProductAnalyticsEvent.builder()
                .eventType("CATEGORY_CREATED")
                .productId(null)
                .timestamp(Instant.now())
                .build());
    }

    private void publishCategoryUpdated(Category before, Category after) {
        // Publish to Kafka
        ProductEvents.ProductLifecycleEvent event = ProductEvents.ProductLifecycleEvent.builder()
                .eventType("CATEGORY_UPDATED")
                .productId(null)
                .timestamp(Instant.now())
                .version(1)
                .build();
        eventPublisher.publishProductAnalytics(ProductEvents.ProductAnalyticsEvent.builder()
                .eventType("CATEGORY_UPDATED")
                .productId(null)
                .timestamp(Instant.now())
                .build());
    }

    private void publishCategoryDeleted(Category category) {
        // Publish to Kafka
        ProductEvents.ProductLifecycleEvent event = ProductEvents.ProductLifecycleEvent.builder()
                .eventType("CATEGORY_DELETED")
                .productId(null)
                .timestamp(Instant.now())
                .version(1)
                .build();
        eventPublisher.publishProductAnalytics(ProductEvents.ProductAnalyticsEvent.builder()
                .eventType("CATEGORY_DELETED")
                .productId(null)
                .timestamp(Instant.now())
                .build());
    }

    private void invalidateCategoryCache(Category category) {
        // Invalidate category-related caches
        // This would be more comprehensive in production
        log.info("Invalidating category cache for: {}", category.getId());
    }
}
