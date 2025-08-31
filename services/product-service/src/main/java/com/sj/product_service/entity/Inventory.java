package com.sj.product_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "inventories", uniqueConstraints = @UniqueConstraint(columnNames = {"product_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    @Column(nullable = false)
    private int totalQuantity;

    @Column(nullable = false)
    private int reservedQuantity;

    @Column(nullable = false)
    private int reorderLevel;

    @Column(nullable = false)
    private boolean lowStockAlert;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private int version;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        lowStockAlert = totalQuantity <= reorderLevel;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
        lowStockAlert = totalQuantity <= reorderLevel;
    }

    public int getAvailableQuantity() {
        return totalQuantity - reservedQuantity;
    }
}
