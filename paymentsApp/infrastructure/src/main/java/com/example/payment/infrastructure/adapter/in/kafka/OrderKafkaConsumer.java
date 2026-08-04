package com.example.payment.infrastructure.adapter.in.kafka;

import com.example.payment.application.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderKafkaConsumer {

    private final PaymentService paymentService;

    @KafkaListener(topics = "order-events", groupId = "payment-group")
    public void listenOrderCreated(OrderCreatedEvent event) {
        log.info("📥 [Payment-App] Pedido recibido de Kafka con ID: {}", event.orderId());

        // Procesar y guardar en base de datos
        paymentService.processPayment(event.orderId(), event.amount());

        log.info("💾 [Payment-App] Pago guardado en PostgreSQL para el pedido: {}", event.orderId());
    }
}