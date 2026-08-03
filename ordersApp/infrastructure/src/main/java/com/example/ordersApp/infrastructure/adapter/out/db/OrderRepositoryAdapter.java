package com.example.ordersApp.infrastructure.adapter.out.db;

import com.example.ordersApp.application.port.out.OrderRepositoryPort;
import com.example.ordersApp.domain.model.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderRepositoryAdapter implements OrderRepositoryPort {

    private final OrderJpaRepository jpaRepository;

    @Override
    public Order save(Order order) {
        OrderJpaEntity entity = OrderJpaEntity.builder()
                .id(order.getId())
                .customerId(order.getCustomerId())
                .amount(order.getAmount())
                .status(order.getStatus())
                .build();

        OrderJpaEntity savedEntity = jpaRepository.save(entity);

        return Order.builder()
                .id(savedEntity.getId())
                .customerId(savedEntity.getCustomerId())
                .amount(savedEntity.getAmount())
                .status(savedEntity.getStatus())
                .build();
    }
}
