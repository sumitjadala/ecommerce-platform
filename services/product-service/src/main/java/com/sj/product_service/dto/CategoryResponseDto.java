package com.sj.product_service.dto;

import com.sj.product_service.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponseDto {

    private UUID id;
    private String name;
    private String slug;
    private String description;
    private UUID parentId;
    private String hierarchyPath;
    private Integer level;
    private Integer leftBound;
    private Integer rightBound;
    private Integer sortOrder;
    private Boolean isActive;
    private String seoTitle;
    private String seoDescription;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CategoryResponseDto> children;
    private Long productCount;

    public static CategoryResponseDto fromEntity(Category category) {
        return CategoryResponseDto.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .hierarchyPath(category.getHierarchyPath())
                .level(category.getLevel())
                .leftBound(category.getLeftBound())
                .rightBound(category.getRightBound())
                .sortOrder(category.getSortOrder())
                .isActive(category.getIsActive())
                .seoTitle(category.getSeoTitle())
                .seoDescription(category.getSeoDescription())
                .imageUrl(category.getImageUrl())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
