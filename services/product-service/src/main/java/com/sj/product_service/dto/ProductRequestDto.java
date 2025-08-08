package com.sj.product_service.dto;

import com.sj.product_service.entity.Product;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequestDto {

    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 500, message = "Product name must be between 2 and 500 characters")
    private String name;

    @NotBlank(message = "SKU is required")
    @Size(min = 3, max = 100, message = "SKU must be between 3 and 100 characters")
    private String sku;

    @NotBlank(message = "Product slug is required")
    @Size(min = 2, max = 500, message = "Product slug must be between 2 and 500 characters")
    private String slug;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    @Size(max = 1000, message = "Short description cannot exceed 1000 characters")
    private String shortDescription;

    @NotNull(message = "Product price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    @DecimalMin(value = "0.0", message = "Cost price cannot be negative")
    private BigDecimal costPrice;

    @Size(max = 3, message = "Currency code must be 3 characters")
    private String currency = "USD";

    private Product.ProductStatus status = Product.ProductStatus.DRAFT;

    @DecimalMin(value = "0.0", message = "Weight cannot be negative")
    private BigDecimal weight;

    private String dimensions; // JSON: {length, width, height, unit}

    private List<String> tags;

    @Size(max = 200, message = "SEO title cannot exceed 200 characters")
    private String seoTitle;

    @Size(max = 500, message = "SEO description cannot exceed 500 characters")
    private String seoDescription;

    private List<String> metaKeywords;

    private Boolean featured = false;

    private Boolean digitalProduct = false;

    private UUID sellerId;
}
