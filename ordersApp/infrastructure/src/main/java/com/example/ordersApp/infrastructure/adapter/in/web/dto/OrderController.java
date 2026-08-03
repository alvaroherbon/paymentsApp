package com.example.ordersApp.infrastructure.adapter.in.web.dto;

import com.example.ordersApp.application.port.in.CreateOrderUseCase;
import com.example.ordersApp.domain.model.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final CreateOrderUseCase createOrderUseCase;

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody CreateOrderRequest request) {
        Order order = createOrderUseCase.createOrder(request.customerId(), request.amount());
        return ResponseEntity.ok(order);
    }
}
