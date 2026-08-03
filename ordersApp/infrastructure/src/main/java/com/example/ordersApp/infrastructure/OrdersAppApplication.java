package com.example.ordersApp.infrastructure;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.example.ordersApp")
public class OrdersAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrdersAppApplication.class, args);
    }
}
