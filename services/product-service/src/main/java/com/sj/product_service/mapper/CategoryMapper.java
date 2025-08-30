package com.sj.product_service.mapper;

import com.sj.product_service.dto.CategoryRequest;
import com.sj.product_service.dto.CategoryResponse;
import com.sj.product_service.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryResponse toResponse(Category category) {
        if (category == null) return null;
        // Fetch children via repository (assume it's injected)

        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .hierarchyPath(category.getHierarchyPath())
                .level(category.getLevel())
                .isActive(category.getIsActive())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .createdBy(category.getCreatedBy())
                .updatedBy(category.getUpdatedBy())
                .build();
    }


    public Category toEntity(CategoryRequest request, Category parent) {
        if (request == null) return null;
        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        category.setParent(parent);
        category.setSlug(null); // Slug set in service layer after name/slug processing
        category.setHierarchyPath(null); // Set in service layer
        category.setLevel(null); // Set in service layer
        return category;
    }

    public void updateEntity(Category category, CategoryRequest request, Category parent) {
        if (category == null || request == null) return;
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setIsActive(request.getIsActive() != null ? request.getIsActive() : category.getIsActive());
        category.setParent(parent);
        // Update slug, hierarchyPath, level handled separately in service
    }
}