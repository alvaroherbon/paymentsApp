package com.example.payment.infrastructure.adapter.out.persistence;

import com.example.payment.application.port.out.PaymentRepositoryPort;
import com.example.payment.domain.model.Payment;
import com.example.payment.infrastructure.adapter.out.persistence.entity.PaymentEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentPersistenceAdapter implements PaymentRepositoryPort {

    private final SpringDataPaymentRepository repository;

    @Override
    public Payment save(Payment payment) {
        PaymentEntity entity = new PaymentEntity(
                payment.getId(),
                payment.getOrderId(),
                payment.getAmount(),
                payment.getStatus()
        );

        PaymentEntity saved = repository.save(entity);

        return new Payment(
                saved.getId(),
                saved.getOrderId(),
                saved.getAmount(),
                saved.getStatus()
        );
    }
}