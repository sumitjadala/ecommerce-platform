package com.sj.product_service.repository;

import com.sj.product_service.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    Optional<Category> findBySlug(String slug);

    List<Category> findByParentId(UUID parentId);

    List<Category> findByIsActiveTrueOrderByNameAsc();

    List<Category> findByParentIsNull();

    List<Category> findByParent(Category category);
}
