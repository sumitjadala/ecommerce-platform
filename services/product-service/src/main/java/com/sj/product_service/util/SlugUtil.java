package com.sj.product_service.util;

import java.util.UUID;

public class SlugUtil {
    public static String toSlug(String input) {
        if (input == null) {
            return "";
        }
        String slug = input.toLowerCase();
        slug = java.text.Normalizer.normalize(slug, java.text.Normalizer.Form.NFD);
        slug = slug.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        slug = slug.replaceAll("[^a-z0-9\\s-]", "");
        slug = slug.trim().replaceAll("[\\s-]+", "-");

        int maxLength = 40; // reserve some space for UUID part
        if (slug.length() > maxLength) {
            slug = slug.substring(0, maxLength);
        }
        String uuidSuffix = UUID.randomUUID().toString().substring(0, 8);
        return slug + "-" + uuidSuffix;
    }
}
