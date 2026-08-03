package com.example.ordersApp.application.port.out;

import com.example.ordersApp.domain.model.Order;

public interface OrderEventPublisherPort {
    void publishOrderCreated(Order order);
}
