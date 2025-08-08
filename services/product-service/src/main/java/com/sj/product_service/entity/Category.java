package com.sj.product_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "categories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 255, message = "Category name must be between 2 and 255 characters")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Category slug is required")
    @Size(min = 2, max = 255, message = "Category slug must be between 2 and 255 characters")
    @Column(nullable = false, unique = true)
    private String slug;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Category> children;

    @Size(max = 1000, message = "Hierarchy path cannot exceed 1000 characters")
    @Column(name = "hierarchy_path")
    private String hierarchyPath; // e.g., "/electronics/phones/smartphones"

    @Min(value = 0, message = "Level cannot be negative")
    @Column(nullable = false)
    private Integer level = 0;

    @NotNull(message = "Left bound is required")
    @Column(name = "left_bound", nullable = false)
    private Integer leftBound;

    @NotNull(message = "Right bound is required")
    @Column(name = "right_bound", nullable = false)
    private Integer rightBound;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Size(max = 200, message = "SEO title cannot exceed 200 characters")
    @Column(name = "seo_title")
    private String seoTitle;

    @Size(max = 500, message = "SEO description cannot exceed 500 characters")
    @Column(name = "seo_description")
    private String seoDescription;

    @Size(max = 500, message = "Image URL cannot exceed 500 characters")
    @Column(name = "image_url")
    private String imageUrl;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
