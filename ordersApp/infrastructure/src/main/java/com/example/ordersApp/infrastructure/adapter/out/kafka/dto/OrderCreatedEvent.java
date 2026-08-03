package com.example.ordersApp.infrastructure.adapter.out.kafka.dto;

import com.example.ordersApp.domain.enums.OrderStatus;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

public record OrderCreatedEvent(
    UUID orderId,
    String customerId,
    BigDecimal amount,
    OrderStatus status) implements Serializable{}

