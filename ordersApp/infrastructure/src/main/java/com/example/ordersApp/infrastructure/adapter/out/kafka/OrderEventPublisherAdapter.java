package com.example.ordersApp.infrastructure.adapter.out.kafka;

import com.example.ordersApp.application.port.out.OrderEventPublisherPort;
import com.example.ordersApp.domain.model.Order;
import com.example.ordersApp.infrastructure.adapter.out.kafka.dto.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

@Slf4j
@ConditionalOnBean(KafkaTemplate.class)
@Component
@RequiredArgsConstructor
public class OrderEventPublisherAdapter implements OrderEventPublisherPort {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "order-events";

    @Override
    public void publishOrderCreated(Order order) {
        OrderCreatedEvent event = new OrderCreatedEvent(
                order.getId(),
                order.getCustomerId(),
                order.getAmount(),
                order.getStatus()
        );

        log.info("Publicando evento OrderCreatedEvent en Kafka para el pedido: {}", order.getId());
        kafkaTemplate.send(TOPIC, order.getId().toString(), event);
    }
}
