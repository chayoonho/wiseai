package com.example.wiseai_dev.payment.domain.repository;

import com.example.wiseai_dev.payment.domain.model.Payment;

import java.util.Optional;

public interface PaymentRepository {
    Payment save(Payment payment);

    Optional<Payment> findById(Long id);

    Optional<Object> findByReservationId(Long reservationId);
}
