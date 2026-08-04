package com.example.payment.infrastructure;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@SpringBootApplication(scanBasePackages = "com.example.payment")
public class PaymentApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaymentApplication.class, args);
    }

    @Bean
    public CommandLineRunner checkKafkaBeans(ApplicationContext ctx) {
        return args -> {
            boolean hasListenerFactory = ctx.containsBean("kafkaListenerContainerFactory");
            System.out.println("👉 ¿Existe el contenedor de Kafka en Spring?: " + hasListenerFactory);
        };
    }
}