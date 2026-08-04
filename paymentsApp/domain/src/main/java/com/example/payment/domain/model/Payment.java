package com.example.payment.domain.model;

import com.example.payment.domain.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Payment {

    private UUID id;
    private UUID orderId;
    private BigDecimal amount;
    private OrderStatus status;

}