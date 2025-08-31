package com.sj.product_service.service.impl;

import com.sj.product_service.dto.ProductRequestDto;
import com.sj.product_service.dto.ProductResponseDto;
import com.sj.product_service.entity.Category;
import com.sj.product_service.entity.Product;
import com.sj.product_service.entity.ProductImage;
import com.sj.product_service.repository.CategoryRepository;
import com.sj.product_service.repository.ProductImageRepository;
import com.sj.product_service.repository.ProductRepository;
import com.sj.product_service.service.InventoryService;
import com.sj.product_service.service.ProductService;
import com.sj.product_service.service.S3Service;
import com.sj.product_service.util.SlugUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ProductImageRepository productImageRepository;
    private final CategoryRepository categoryRepository;

    private final InventoryService inventoryService;
    private final S3Service s3Service;
    @Value("${aws.s3.bucket}")
    private String bucketName;
    @Override
    public ProductResponseDto createProduct(ProductRequestDto productRequestDto) {
        log.info("Creating product: {}", productRequestDto.getName());

        Set<Category> categories = new HashSet<>();
        if (productRequestDto.getCategories() != null) {
            categories = new HashSet<>(categoryRepository.findAllById(productRequestDto.getCategories()));
        }

        Product product = Product.builder()
                .sellerId(productRequestDto.getSellerId())
                .name(productRequestDto.getName())
                .description(productRequestDto.getDescription())
                .shortDescription(productRequestDto.getShortDescription())
                .price(productRequestDto.getPrice())
                .costPrice(productRequestDto.getCostPrice())
                .currency(productRequestDto.getCurrency())
                .status(productRequestDto.getStatus())
                .weight(productRequestDto.getWeight())
                .dimensions(productRequestDto.getDimensions())
                .tags(productRequestDto.getTags())
                .featured(productRequestDto.getFeatured())
                .slug(SlugUtil.toSlug(productRequestDto.getName()))
                .categories(categories)
                .build();

        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with ID: {}", savedProduct.getId());
        inventoryService.createInventoryForProductWithStock(savedProduct,
                productRequestDto.getInitialStock(),
                productRequestDto.getReorderLevel());
        return ProductResponseDto.fromEntity(savedProduct);
    }

    @Override
    public ProductResponseDto updateProduct(UUID id, ProductRequestDto productRequestDto) {
        log.info("Updating product: {}", id);

        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));

        existingProduct.setName(productRequestDto.getName());
        existingProduct.setDescription(productRequestDto.getDescription());
        existingProduct.setShortDescription(productRequestDto.getShortDescription());
        existingProduct.setPrice(productRequestDto.getPrice());
        existingProduct.setCostPrice(productRequestDto.getCostPrice());
        existingProduct.setCurrency(productRequestDto.getCurrency());
        existingProduct.setStatus(productRequestDto.getStatus());
        existingProduct.setWeight(productRequestDto.getWeight());
        existingProduct.setDimensions(productRequestDto.getDimensions());
        existingProduct.setTags(productRequestDto.getTags());
        existingProduct.setFeatured(productRequestDto.getFeatured());

        Product updatedProduct = productRepository.save(existingProduct);
        log.info("Product updated successfully: {}", id);
        return ProductResponseDto.fromEntity(updatedProduct);
    }

    @Override
    public ProductResponseDto getProductById(UUID id) {
        log.info("Getting product by ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));

        ProductResponseDto dto = ProductResponseDto.fromEntity(product);
        return dto;
    }


    @Override
    public Page<ProductResponseDto> searchProducts(String searchTerm, Pageable pageable) {
        log.info("Searching products with term: {}", searchTerm);

        Page<Product> products = productRepository.searchProducts(searchTerm, pageable);
        return products.map(ProductResponseDto::fromEntity);
    }

    @Override
    public List<ProductResponseDto> getAvailableProducts() {
        log.info("Getting available products");

        List<Product> products = productRepository.findActiveProducts();
        return products.stream()
                .map(ProductResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ProductResponseDto> getProductsBySeller(UUID sellerId, Pageable pageable) {
        log.info("Getting products by seller: {}", sellerId);

        Page<Product> products = productRepository.findBySellerId(sellerId, pageable);
        return products.map(ProductResponseDto::fromEntity);
    }
    @Override
    public void deleteProduct(UUID id) {
        log.info("Deleting product: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));

        product.setStatus(Product.ProductStatus.ARCHIVED);
        productRepository.save(product);
        log.info("Product deleted successfully: {}", id);
    }

    @Override
    public void updateProductStatus(UUID productId, Product.ProductStatus status) {
        log.info("Updating product status: {} to {}", productId, status);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
        product.setStatus(status);
        productRepository.save(product);
        log.info("Product status updated successfully: {} to {}", productId, status);
    }

    @Override
    @Transactional
    public Product saveProductWithImage(String productId, MultipartFile image) throws IOException {
        UUID productUuid = UUID.fromString(productId);
        Product product = productRepository.findById(productUuid)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with ID: " + productId));

        String s3Key = "products/" + product.getId() + "/" + UUID.randomUUID() + "-" + image.getOriginalFilename();

        s3Service.uploadFile(bucketName, s3Key, image);
        String cdnUrl = s3Service.constructCdnUrl(s3Key);

        ProductImage productImage = new ProductImage();
        productImage.setProduct(product);
        productImage.setImageType("Original");
        productImage.setS3Key(s3Key);
        productImage.setS3Bucket(bucketName);
        productImage.setFileName(image.getOriginalFilename());
        productImage.setCdnUrl(cdnUrl);
        productImageRepository.save(productImage);
        return product;
    }

}
