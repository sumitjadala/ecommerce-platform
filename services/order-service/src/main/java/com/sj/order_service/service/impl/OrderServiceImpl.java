package com.sj.order_service.service.impl;

import com.sj.order_service.dto.OrderItemRequest;
import com.sj.order_service.dto.OrderRequest;
import com.sj.order_service.entity.Order;
import com.sj.order_service.entity.OrderItem;
import com.sj.order_service.repository.OrderRepository;
import com.sj.order_service.service.OrderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public Order createOrder(String token, OrderRequest orderRequest) {
        List<OrderItem> orderItems = orderRequest.getItems().stream()
                .map(this::mapToOrderItem)
                .collect(Collectors.toList());

        BigDecimal totalPrice = orderItems.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Check and decrement inventory stock for each item before saving order
//    for (OrderItem item : orderItems) {
//        boolean stockUpdated = inventoryClient.decrementStock(token, item.getProductId(), item.getQuantity());
//        if (!stockUpdated) {
//            throw new InsufficientStockException("Insufficient stock for product ID " + item.getProductId());
//        }
//    }

        // Use builder pattern to create Order
        Order order = Order.builder()
                .customerId(orderRequest.getCustomerId())
                .sellerId(orderRequest.getSellerId())
                .paymentStatus(orderRequest.getPaymentStatus())
                .orderStatus("NEW")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .totalPrice(totalPrice)
                .build();

        // Set items and maintain bidirectional relationship
        order.setItems(orderItems);

        return orderRepository.save(order);
    }


    private OrderItem mapToOrderItem(OrderItemRequest itemRequest) {
        return OrderItem.builder()
                .productId(itemRequest.getProductId())
                .quantity(itemRequest.getQuantity())
                .price(itemRequest.getPrice())
                .build();
    }

}