package com.sj.product_service.service;

import com.sj.product_service.dto.CategoryRequest;
import com.sj.product_service.dto.CategoryResponse;

import java.util.List;
import java.util.UUID;

public interface CategoryService {
    CategoryResponse createCategory(CategoryRequest categoryRequest);

    CategoryResponse updateCategory(UUID id, CategoryRequest categoryRequest);

    void deleteCategory(UUID id);

    CategoryResponse findById(UUID id);

    List<CategoryResponse> getAllCategories(boolean includeInactive);

    List<CategoryResponse> getChildCategories(UUID parentId);

    List<CategoryResponse> getActiveCategories();

    List<CategoryResponse> fetchCategoryTree();
}
