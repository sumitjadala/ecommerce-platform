package com.sj.product_service.cache;

public final class CacheKeys {
    private CacheKeys() {}

    public static String product(String productId) {
        return "product:" + productId;
    }

    public static String categoryProducts(String categorySlug, int page) {
        return "category:" + categorySlug + ":products:page:" + page;
    }

    public static String sellerProducts(String sellerId, int page) {
        return "seller:" + sellerId + ":products:page:" + page;
    }

    public static String inventory(String productId) {
        return "inventory:" + productId;
    }

    public static String search(String queryHash) {
        return "search:" + queryHash;
    }

    public static String categoryFilters(String categoryId) {
        return "category:" + categoryId + ":filters";
    }
}
