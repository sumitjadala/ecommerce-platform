package com.sj.product_service.service;

import com.sj.product_service.dto.CategoryRequestDto;
import com.sj.product_service.dto.CategoryResponseDto;
import com.sj.product_service.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface CategoryService {

    CategoryResponseDto createCategory(CategoryRequestDto categoryRequestDto);

    CategoryResponseDto updateCategory(UUID id, CategoryRequestDto categoryRequestDto);

    CategoryResponseDto getCategoryById(UUID id);

    CategoryResponseDto getCategoryBySlug(String slug);

    Page<CategoryResponseDto> getAllCategories(Pageable pageable);

    List<CategoryResponseDto> getRootCategories();

    List<CategoryResponseDto> getSubCategories(UUID parentId);

    List<CategoryResponseDto> getCategoryTree();

    List<CategoryResponseDto> getCategoryPath(UUID categoryId);

    List<CategoryResponseDto> getCategoryDescendants(UUID categoryId);

    List<CategoryResponseDto> getCategoryAncestors(UUID categoryId);

    Page<CategoryResponseDto> searchCategories(String searchTerm, Pageable pageable);

    void deleteCategory(UUID id);

    void moveCategory(UUID categoryId, UUID newParentId);

    void updateCategoryStatus(UUID id, Boolean isActive);

    void updateCategoryOrder(UUID id, Integer sortOrder);

    boolean existsByName(String name);

    boolean existsBySlug(String slug);

    Long getProductCountByCategory(UUID categoryId);

    List<CategoryResponseDto> getActiveCategories();

    CategoryResponseDto getCategoryWithChildren(UUID id);
}
