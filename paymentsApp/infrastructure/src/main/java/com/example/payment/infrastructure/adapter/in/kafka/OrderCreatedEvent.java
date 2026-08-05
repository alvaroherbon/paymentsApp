package com.example.payment.infrastructure.adapter.in.kafka;

import com.example.payment.domain.enums.OrderStatus;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

public record OrderCreatedEvent(
    UUID orderId,
    String customerId,
    BigDecimal amount) implements Serializable {
}