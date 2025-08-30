package com.sj.product_service.dto;

import com.sj.product_service.entity.Category;
import com.sj.product_service.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDto {

    private UUID id;
    private UUID sellerId;
    private String name;
    private String description;
    private String shortDescription;
    private BigDecimal price;
    private BigDecimal costPrice;
    private String currency;
    private Product.ProductStatus status;
    private BigDecimal weight;
    private String dimensions;
    private List<String> tags;
    private Set<Category> categories;
    private Boolean featured;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID createdBy;
    private UUID updatedBy;

    public static ProductResponseDto fromEntity(Product product) {
        return ProductResponseDto.builder()
                .id(product.getId())
                .sellerId(product.getSellerId())
                .name(product.getName())
                .description(product.getDescription())
                .shortDescription(product.getShortDescription())
                .price(product.getPrice())
                .costPrice(product.getCostPrice())
                .currency(product.getCurrency())
                .status(product.getStatus())
                .weight(product.getWeight())
                .dimensions(product.getDimensions())
                .tags(product.getTags())
                .categories(product.getCategories())
                .featured(product.getFeatured())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .createdBy(product.getCreatedBy())
                .updatedBy(product.getUpdatedBy())
                .build();
    }
}
