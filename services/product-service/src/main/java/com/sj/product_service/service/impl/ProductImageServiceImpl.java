package com.sj.product_service.service.impl;

import com.sj.product_service.entity.ProductImage;
import com.sj.product_service.service.ProductImageService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class ProductImageServiceImpl implements ProductImageService {
    @Override
    public List<ProductImage> findAllByProductId(String productId) {
        return Arrays.asList(new ProductImage());
    }
}
