package com.ecommerce.payment.kafka;

import com.ecommerce.payment.model.Payment;
import com.ecommerce.payment.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

    private final PaymentRepository paymentRepository;

    public OrderEventConsumer(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @KafkaListener(topics = "order-events", groupId = "payment-service-spring-group",
                   containerFactory = "kafkaListenerContainerFactory")
    public void consumeOrderEvent(List<Map<String, Object>> records, Acknowledgment ack) {
        for (Map<String, Object> payload : records) {
            try {
                processRecord(payload);
            } catch (Exception e) {
                log.error("Error procesando mensaje: {}", payload, e);
            }
        }
        ack.acknowledge();
    }

    @SuppressWarnings("unchecked")
    private void processRecord(Map<String, Object> payload) {
        Integer orderId = (Integer) payload.get("order_id");
        Object totalPrice = payload.get("total_price");

        if (orderId == null || totalPrice == null) {
            log.warn("Mensaje sin campos requeridos (order_id={}, total_price={}) — saltando", orderId, totalPrice);
            return;
        }

        BigDecimal amount = new BigDecimal(String.valueOf(totalPrice));

        if (paymentRepository.existsByOrderId(orderId)) {
            log.warn("El pago para el Pedido #{} ya había sido procesado.", orderId);
            return;
        }

        Payment payment = new Payment(orderId, amount, "COMPLETED");
        paymentRepository.save(payment);
        log.info("Pago registrado correctamente para el Pedido #{}", orderId);
    }
}
