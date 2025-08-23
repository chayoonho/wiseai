package com.example.wiseai_dev.payment.infrastructure.persistence.jpa;

import com.example.wiseai_dev.payment.infrastructure.persistence.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, Long> {
    Optional<PaymentEntity> findByReservationId(Long reservationId);
    Optional<Object> findByTransactionId(String transactionId);
    void deleteByReservationId(Long reservationId);
}
