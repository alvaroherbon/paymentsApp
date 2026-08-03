package com.example.ordersApp.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;

public record CreateOrderRequest(String customerId,
                                 BigDecimal amount
) {}