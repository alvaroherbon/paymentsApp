package com.example.ordersApp.infrastructure.config;

import com.example.ordersApp.application.port.in.CreateOrderUseCase;
import com.example.ordersApp.application.port.out.OrderEventPublisherPort;
import com.example.ordersApp.application.port.out.OrderRepositoryPort;
import com.example.ordersApp.application.service.OrderService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    @Bean
    public CreateOrderUseCase createOrderUseCase(
            OrderRepositoryPort orderRepositoryPort,
            ObjectProvider<OrderEventPublisherPort> orderEventPublisherPortProvider) {
        OrderEventPublisherPort publisher = orderEventPublisherPortProvider.getIfAvailable(() -> new com.example.ordersApp.infrastructure.adapter.out.kafka.NoOpOrderEventPublisherAdapter());
        return new OrderService(orderRepositoryPort, publisher);
    }

}
