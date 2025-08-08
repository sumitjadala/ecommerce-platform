package com.sj.product_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "inventory")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    private ProductVariant variant;

    @Column(name = "location_id")
    private UUID locationId; // For multi-warehouse support

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    @Column(nullable = false)
    private Integer quantity = 0;

    @Min(value = 0, message = "Reserved quantity cannot be negative")
    @Column(name = "reserved_quantity")
    private Integer reservedQuantity = 0; // For pending orders

    @Column(name = "available_quantity")
    private Integer availableQuantity; // Generated column: quantity - reserved_quantity

    @Min(value = 0, message = "Reorder level cannot be negative")
    @Column(name = "reorder_level")
    private Integer reorderLevel = 5; // Auto-alert threshold

    @Min(value = 0, message = "Max level cannot be negative")
    @Column(name = "max_level")
    private Integer maxLevel; // Maximum stock level

    @Column(name = "last_restocked_at")
    private LocalDateTime lastRestockedAt;

    @LastModifiedDate
    @Column(name = "last_updated_at", nullable = false)
    private LocalDateTime lastUpdatedAt;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        lastUpdatedAt = LocalDateTime.now();
        // Calculate available quantity
        if (quantity != null && reservedQuantity != null) {
            availableQuantity = quantity - reservedQuantity;
        }
    }
}
