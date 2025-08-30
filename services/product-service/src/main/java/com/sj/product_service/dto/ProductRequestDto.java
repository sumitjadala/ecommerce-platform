package com.sj.product_service.dto;

import com.sj.product_service.entity.Product;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequestDto {

    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 500, message = "Product name must be between 2 and 500 characters")
    private String name;

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

    private String dimensions;

    private List<String> tags;

    private Boolean featured = false;

    private UUID sellerId;

    @NotNull(message = "Product category is required")
    private Set<UUID> categories;
}
