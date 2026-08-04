package com.example.ordersApp.application.service;

import com.example.ordersApp.application.port.in.CreateOrderUseCase;
import com.example.ordersApp.application.port.out.OrderEventPublisherPort;
import com.example.ordersApp.application.port.out.OrderRepositoryPort;
import com.example.ordersApp.domain.enums.OrderStatus;
import com.example.ordersApp.domain.model.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.logging.structured.ElasticCommonSchemaProperties;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService implements CreateOrderUseCase {

    private final OrderRepositoryPort orderRepositoryPort;
    private final OrderEventPublisherPort orderEventPublisherPort;

    @Override
    public Order createOrder(String customerId, BigDecimal amount) {
        Order order = Order.builder().id(UUID.randomUUID()).customerId(customerId).amount(amount).status(OrderStatus.PENDING).build();

        Order savedOrder = orderRepositoryPort.save(order);

        orderEventPublisherPort.publishOrderCreated(savedOrder);

        return savedOrder;
    }
}
