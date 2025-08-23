package com.sj.product_service.service;

import com.sj.product_service.dto.ProductRequestDto;
import com.sj.product_service.dto.ProductResponseDto;
import com.sj.product_service.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface ProductService {
    ProductResponseDto createProduct(ProductRequestDto productRequestDto);
    ProductResponseDto updateProduct(UUID id, ProductRequestDto productRequestDto);
    ProductResponseDto getProductById(UUID id);
    Page<ProductResponseDto> searchProducts(String searchTerm, Pageable pageable);
    List<ProductResponseDto> getAvailableProducts();
    Page<ProductResponseDto> getProductsBySeller(UUID sellerId, Pageable pageable);
    void deleteProduct(UUID id);
    void updateProductStatus(UUID productId, Product.ProductStatus status);
    Product saveProductWithImage(String productDTO, MultipartFile imageFile) throws IOException;
}
