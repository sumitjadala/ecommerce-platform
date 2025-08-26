package com.sj.product_service.service;

import com.sj.product_service.entity.ProductImage;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ProductImageService {

    List<ProductImage> findAllByProductId(String productId);

}
