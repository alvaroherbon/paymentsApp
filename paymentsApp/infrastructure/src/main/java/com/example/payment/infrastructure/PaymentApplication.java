package com.example.payment.infrastructure;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;

@EnableKafka
@SpringBootApplication(scanBasePackages = "com.example.payment")
public class PaymentApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaymentApplication.class, args);
    }

    @Bean
    public CommandLineRunner verifyListenerRegistry(KafkaListenerEndpointRegistry registry) {
        return args -> {
            System.out.println("🔍 Total de listeners de Kafka registrados en el registro: " + registry.getListenerContainerIds().size());
            for (String id : registry.getListenerContainerIds()) {
                System.out.println("👉 Listener ID activo: " + id);
            }
        };
    }
}