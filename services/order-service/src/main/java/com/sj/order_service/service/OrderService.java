package com.sj.order_service.service;

import com.sj.order_service.dto.OrderRequest;
import com.sj.order_service.entity.Order;
import org.springframework.stereotype.Service;

@Service
public interface OrderService {
    Order createOrder(String token, OrderRequest orderRequest);
}