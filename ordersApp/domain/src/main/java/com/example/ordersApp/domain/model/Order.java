package com.example.ordersApp.domain.model;

import com.example.ordersApp.domain.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class Order {
    private UUID id;
    private String customerId;
    private BigDecimal amount;
    private OrderStatus status;
}
