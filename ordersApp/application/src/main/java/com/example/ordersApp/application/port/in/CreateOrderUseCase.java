package com.example.ordersApp.application.port.in;

import com.example.ordersApp.domain.model.Order;

import java.math.BigDecimal;

public interface CreateOrderUseCase {
    Order createOrder(String customerId, BigDecimal amount);
}
