package com.sj.order_service.client;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class InventoryClient {

    private final WebClient webClient;

    public InventoryClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://product-service/api/inventory").build();
    }

    /**
     * Decrements stock by calling product-service with provided token for authorization.
     * 
     * @param token Bearer JWT token from UI/user
     * @param productId Product to decrement
     * @param quantity Quantity to decrement
     * @return true if success, false if insufficient stock
     */
    public boolean decrementStock(String token, Long productId, int quantity) {
        return webClient.post()
            .uri("/decrement/{productId}", productId)
            .header("Authorization", "Bearer " + token)
            .bodyValue(quantity)
            .retrieve()
            .bodyToMono(Boolean.class)
            .block();
    }
}