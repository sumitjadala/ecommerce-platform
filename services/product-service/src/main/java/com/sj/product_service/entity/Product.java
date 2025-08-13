package com.sj.product_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull(message = "Seller ID is required")
    @Column(name = "seller_id", nullable = false)
    private UUID sellerId;

    @NotBlank(message = "SKU is required")
    @Size(min = 3, max = 100, message = "SKU must be between 3 and 100 characters")
    @Column(nullable = false, unique = true)
    private String sku;

    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 500, message = "Product name must be between 2 and 500 characters")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Product slug is required")
    @Size(min = 2, max = 500, message = "Product slug must be between 2 and 500 characters")
    @Column(nullable = false, unique = true)
    private String slug;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    @Column(columnDefinition = "TEXT")
    private String description;

    @Size(max = 1000, message = "Short description cannot exceed 1000 characters")
    @Column(name = "short_description")
    private String shortDescription;

    @NotNull(message = "Product price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @DecimalMin(value = "0.0", message = "Cost price cannot be negative")
    @Column(name = "cost_price", precision = 10, scale = 2)
    private BigDecimal costPrice;

    @Size(max = 3, message = "Currency code must be 3 characters")
    @Column(length = 3)
    private String currency = "USD";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status = ProductStatus.DRAFT;

    @DecimalMin(value = "0.0", message = "Weight cannot be negative")
    @Column(precision = 8, scale = 3)
    private BigDecimal weight;

    @Column
    private String dimensions;

    @Column(columnDefinition = "TEXT[]")
    private List<String> tags;

    @Size(max = 200, message = "SEO title cannot exceed 200 characters")
    @Column(name = "seo_title")
    private String seoTitle;

    @Size(max = 500, message = "SEO description cannot exceed 500 characters")
    @Column(name = "seo_description")
    private String seoDescription;

    @Column(name = "meta_keywords", columnDefinition = "TEXT[]")
    private List<String> metaKeywords;

    @Column(name = "featured")
    private Boolean featured = false;

    @Column(name = "digital_product")
    private Boolean digitalProduct = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum ProductStatus {
        DRAFT, ACTIVE, INACTIVE, ARCHIVED
    }
}
