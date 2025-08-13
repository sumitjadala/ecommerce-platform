package com.sj.product_service.security;

import com.sj.product_service.entity.Product;
import com.sj.product_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductOwnershipValidator {

    private final ProductRepository productRepository;

    public boolean isOwnerOrAdmin(UUID productId, Authentication authentication) {
        log.debug("Checking ownership for product: {} and user: {}", productId, authentication.getName());

        // Check if user is admin
        if (hasRole(authentication, "ADMIN")) {
            log.debug("User is admin, access granted");
            return true;
        }

        // Get product and check ownership
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            log.debug("Product not found: {}", productId);
            return false;
        }

        // Extract seller ID from authentication
        String currentUserId = extractUserIdFromAuth(authentication);
        if (currentUserId == null) {
            log.debug("Could not extract user ID from authentication");
            return false;
        }

        boolean isOwner = product.getSellerId().toString().equals(currentUserId);
        log.debug("User {} is owner of product {}: {}", currentUserId, productId, isOwner);
        
        return isOwner;
    }

    public boolean canViewProduct(Product product, Authentication authentication) {
        log.debug("Checking view permission for product: {} and user: {}", product.getId(), authentication.getName());

        // Admins can view everything
        if (hasRole(authentication, "ADMIN")) {
            log.debug("User is admin, view access granted");
            return true;
        }

        // Public products can be viewed by anyone
        if (Product.ProductStatus.ACTIVE.equals(product.getStatus())) {
            log.debug("Product is active, public view access granted");
            return true;
        }

        // Draft/inactive products only by owner
        String currentUserId = extractUserIdFromAuth(authentication);
        if (currentUserId == null) {
            log.debug("Could not extract user ID from authentication");
            return false;
        }

        boolean isOwner = product.getSellerId().toString().equals(currentUserId);
        log.debug("User {} is owner of product {}: {}", currentUserId, product.getId(), isOwner);
        
        return isOwner;
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals("ROLE_" + role));
    }

    private String extractUserIdFromAuth(Authentication authentication) {
        return "00000000-0000-0000-0000-000000000001";
    }
}
