package com.sj.product_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "product_variants")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotBlank(message = "Variant SKU is required")
    @Size(min = 3, max = 100, message = "Variant SKU must be between 3 and 100 characters")
    @Column(nullable = false, unique = true)
    private String sku;

    @NotBlank(message = "Variant name is required")
    @Size(min = 2, max = 255, message = "Variant name must be between 2 and 255 characters")
    @Column(nullable = false)
    private String name; // e.g., "Red - Large"

    @DecimalMin(value = "0.0", message = "Price cannot be negative")
    @Column(precision = 10, scale = 2)
    private BigDecimal price; // Override parent price if needed

    @DecimalMin(value = "0.0", message = "Cost price cannot be negative")
    @Column(name = "cost_price", precision = 10, scale = 2)
    private BigDecimal costPrice;

    @DecimalMin(value = "0.0", message = "Weight cannot be negative")
    @Column(precision = 8, scale = 3)
    private BigDecimal weight;

    @Column(columnDefinition = "JSONB")
    private String attributes; // {color: "red", size: "L", material: "cotton"}

    @Column(name = "is_default")
    private Boolean isDefault = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status = ProductStatus.ACTIVE;

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

    public enum ProductStatus {
        ACTIVE, INACTIVE, OUT_OF_STOCK
    }
}
