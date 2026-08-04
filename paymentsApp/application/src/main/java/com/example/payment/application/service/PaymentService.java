package com.example.payment.application.service;

import com.example.payment.application.port.out.PaymentRepositoryPort;
import com.example.payment.domain.enums.OrderStatus;
import com.example.payment.domain.model.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepositoryPort paymentRepositoryPort;

    public void processPayment(UUID orderId, BigDecimal amount) {
        // Creamos el pago con estado PENDING o SUCCESS directamente para esta prueba
        Payment payment = new Payment(UUID.randomUUID(), orderId, amount, OrderStatus.COMPLETED);
        paymentRepositoryPort.save(payment);
    }
}