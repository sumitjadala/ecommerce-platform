package com.sj.product_service.dto;

import com.sj.product_service.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDto {

    private UUID id;
    private UUID sellerId;
    private String sku;
    private String name;
    private String slug;
    private String description;
    private String shortDescription;
    private BigDecimal price;
    private BigDecimal costPrice;
    private String currency;
    private Product.ProductStatus status;
    private BigDecimal weight;
    private String dimensions;
    private List<String> tags;
    private String seoTitle;
    private String seoDescription;
    private List<String> metaKeywords;
    private Boolean featured;
    private Boolean digitalProduct;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID createdBy;
    private UUID updatedBy;

    public static ProductResponseDto fromEntity(Product product) {
        return ProductResponseDto.builder()
                .id(product.getId())
                .sellerId(product.getSellerId())
                .sku(product.getSku())
                .name(product.getName())
                .slug(product.getSlug())
                .description(product.getDescription())
                .shortDescription(product.getShortDescription())
                .price(product.getPrice())
                .costPrice(product.getCostPrice())
                .currency(product.getCurrency())
                .status(product.getStatus())
                .weight(product.getWeight())
                .dimensions(product.getDimensions())
                .tags(product.getTags())
                .seoTitle(product.getSeoTitle())
                .seoDescription(product.getSeoDescription())
                .metaKeywords(product.getMetaKeywords())
                .featured(product.getFeatured())
                .digitalProduct(product.getDigitalProduct())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .createdBy(product.getCreatedBy())
                .updatedBy(product.getUpdatedBy())
                .build();
    }
}
