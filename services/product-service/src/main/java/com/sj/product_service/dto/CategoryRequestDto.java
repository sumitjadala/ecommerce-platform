package com.sj.product_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequestDto {

    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 255, message = "Category name must be between 2 and 255 characters")
    private String name;

    @NotBlank(message = "Category slug is required")
    @Size(min = 2, max = 255, message = "Category slug must be between 2 and 255 characters")
    private String slug;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    private UUID parentId;

    @Size(max = 1000, message = "Hierarchy path cannot exceed 1000 characters")
    private String hierarchyPath;

    private Integer level = 0;

    private Integer leftBound;

    private Integer rightBound;

    private Integer sortOrder = 0;

    private Boolean isActive = true;

    @Size(max = 200, message = "SEO title cannot exceed 200 characters")
    private String seoTitle;

    @Size(max = 500, message = "SEO description cannot exceed 500 characters")
    private String seoDescription;

    @Size(max = 500, message = "Image URL cannot exceed 500 characters")
    private String imageUrl;
}
