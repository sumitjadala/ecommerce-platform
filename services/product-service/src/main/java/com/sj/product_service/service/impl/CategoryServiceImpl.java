package com.sj.product_service.service.impl;

import com.sj.product_service.dto.CategoryRequest;
import com.sj.product_service.dto.CategoryResponse;
import com.sj.product_service.entity.Category;
import com.sj.product_service.mapper.CategoryMapper;
import com.sj.product_service.repository.CategoryRepository;
import com.sj.product_service.service.CategoryService;
import com.sj.product_service.util.SlugUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public CategoryResponse createCategory(CategoryRequest categoryRequest) {
        Category parent = null;
        if (categoryRequest.getParent() != null) {
            parent = categoryRepository.findById(categoryRequest.getParent())
                    .orElseThrow(() -> new EntityNotFoundException("Parent category not found"));
        }

        Category category = new Category();

        category.setName(categoryRequest.getName());
        category.setDescription(categoryRequest.getDescription());
        category.setIsActive(categoryRequest.getIsActive() != null ? categoryRequest.getIsActive() : true);

        category.setSlug(SlugUtil.toSlug(categoryRequest.getName()));

        category.setParent(parent);

        if (parent == null) {
            category.setHierarchyPath(category.getSlug());
            category.setLevel(0);
        } else {
            category.setHierarchyPath(parent.getHierarchyPath() + "/" + category.getSlug());
            category.setLevel(parent.getLevel() + 1);
        }

        category = categoryRepository.save(category);
        return categoryMapper.toResponse(category);
    }

    @Override
    public CategoryResponse updateCategory(UUID id, CategoryRequest categoryRequest) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found: " + id));

        Category parent = null;
        if (categoryRequest.getParent() != null) {
            parent = categoryRepository.findById(categoryRequest.getParent())
                    .orElseThrow(() -> new EntityNotFoundException("Parent category not found"));
        }

        existing.setName(categoryRequest.getName());
        existing.setDescription(categoryRequest.getDescription());
        existing.setIsActive(categoryRequest.getIsActive() != null ? categoryRequest.getIsActive() : existing.getIsActive());

        existing.setSlug(SlugUtil.toSlug(categoryRequest.getName()));

        existing.setParent(parent);

        if (parent == null) {
            existing.setHierarchyPath(existing.getSlug());
            existing.setLevel(0);
        } else {
            existing.setHierarchyPath(parent.getHierarchyPath() + "/" + existing.getSlug());
            existing.setLevel(parent.getLevel() + 1);
        }

        existing = categoryRepository.save(existing);
        return categoryMapper.toResponse(existing);
    }

    @Override
    public void deleteCategory(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found: " + id));
        category.setIsActive(false);
        categoryRepository.save(category);
    }

    @Override
    public CategoryResponse findById(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found: " + id));
        return categoryMapper.toResponse(category);
    }

    @Override
    public List<CategoryResponse> getAllCategories(boolean includeInactive) {
        List<Category> categories;
        if (includeInactive) {
            categories = categoryRepository.findAll();
        } else {
            categories = categoryRepository.findByIsActiveTrueOrderByNameAsc();
        }
        return categories.stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryResponse> getChildCategories(UUID parentId) {
        return categoryRepository.findByParentId(parentId).stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryResponse> getActiveCategories() {
        return categoryRepository.findByIsActiveTrueOrderByNameAsc().stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<CategoryResponse> fetchCategoryTree() {
        List<Category> roots = categoryRepository.findByParentIsNull();
        return roots.stream()
                .map(this::buildTree)
                .collect(Collectors.toList());
    }

    // Recursive DTO builder
    private CategoryResponse buildTree(Category category) {
        CategoryResponse response = categoryMapper.toResponse(category);
        List<Category> children = categoryRepository.findByParent(category);

        response.setChildrenId(children.stream().map(Category::getId).collect(Collectors.toList()));
        return response;
    }
}