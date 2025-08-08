package com.sj.product_service.repository;

import com.sj.product_service.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    Optional<Category> findByName(String name);

    Optional<Category> findBySlug(String slug);

    List<Category> findByParentIsNull();

    List<Category> findByParentId(UUID parentId);

    List<Category> findByIsActiveTrue();

    List<Category> findByIsActiveTrueOrderBySortOrderAsc();

    @Query("SELECT c FROM Category c WHERE c.parent IS NULL AND c.isActive = true ORDER BY c.sortOrder ASC")
    List<Category> findActiveRootCategories();

    @Query("SELECT c FROM Category c WHERE c.parent.id = :parentId AND c.isActive = true ORDER BY c.sortOrder ASC")
    List<Category> findActiveSubCategories(@Param("parentId") UUID parentId);

    @Query("SELECT c FROM Category c WHERE c.name LIKE %:searchTerm% OR c.description LIKE %:searchTerm%")
    List<Category> searchCategories(@Param("searchTerm") String searchTerm);

    // Nested set model queries for efficient hierarchical operations
    @Query("SELECT c FROM Category c WHERE c.leftBound BETWEEN :leftBound AND :rightBound ORDER BY c.leftBound")
    List<Category> findDescendants(@Param("leftBound") Integer leftBound, @Param("rightBound") Integer rightBound);

    @Query("SELECT c FROM Category c WHERE c.rightBound BETWEEN :leftBound AND :rightBound ORDER BY c.leftBound")
    List<Category> findAncestors(@Param("leftBound") Integer leftBound, @Param("rightBound") Integer rightBound);

    @Query("SELECT c FROM Category c WHERE c.hierarchyPath LIKE %:path% ORDER BY c.hierarchyPath")
    List<Category> findByHierarchyPath(@Param("path") String path);

    boolean existsByName(String name);

    boolean existsBySlug(String slug);

    @Query("SELECT COUNT(c) FROM Category c WHERE c.parent.id = :parentId")
    Long countByParentId(@Param("parentId") UUID parentId);
}
