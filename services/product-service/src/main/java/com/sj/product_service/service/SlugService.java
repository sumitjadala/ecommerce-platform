package com.sj.product_service.service;

import org.springframework.stereotype.Service;

@Service
public class SlugService {

    public String generateProductSlug(String productName, String productId) {
        String base = sanitize(productName);
        if (base.length() > 50) base = base.substring(0, 50);
        return base + "-" + (productId != null && productId.length() >= 8 ? productId.substring(0, 8) : "");
    }

    public String generateCategorySlug(String categoryName, String parentSlug) {
        String slug = sanitize(categoryName);
        return parentSlug != null && !parentSlug.isBlank() ? parentSlug + "/" + slug : slug;
    }

    private String sanitize(String input) {
        return input.toLowerCase().replaceAll("[^a-z0-9\\s]", "").replaceAll("\\s+", "-");
    }
}
