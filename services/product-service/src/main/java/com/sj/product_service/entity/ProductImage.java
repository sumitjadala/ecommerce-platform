package com.sj.product_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "product_images")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Size(max = 20, message = "Image type cannot exceed 20 characters")
    @Column(name = "image_type", nullable = false)
    private String imageType;

    @Size(max = 255, message = "File name cannot exceed 255 characters")
    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Size(max = 500, message = "S3 key cannot exceed 500 characters")
    @Column(name = "s3_key", nullable = false)
    private String s3Key;

    @Size(max = 100, message = "S3 bucket cannot exceed 100 characters")
    @Column(name = "s3_bucket", nullable = false)
    private String s3Bucket;

    @Size(max = 255, message = "Alt text cannot exceed 255 characters")
    @Column(name = "alt_text")
    private String altText;

    @Min(value = 0, message = "File size cannot be negative")
    @Column(name = "file_size")
    private Integer fileSize; // Bytes

    @Min(value = 1, message = "Width must be positive")
    @Column
    private Integer width;

    @Min(value = 1, message = "Height must be positive")
    @Column
    private Integer height;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "is_primary")
    private Boolean isPrimary = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
