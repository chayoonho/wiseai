package com.example.wiseai_dev.payment.domain.repository;

import com.example.wiseai_dev.payment.domain.model.Payment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PaymentRepository {
    Payment save(Payment payment);
    Optional<Payment> findById(Long id);
    Optional<Payment> findByReservationId(Long reservationId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PaymentEntity p where p.reservation.id = :reservationId")
    Optional<Payment> findByReservationIdForUpdate(@Param("reservationId") Long reservationId);
    Optional<Payment> findByTransactionId(String transactionId);
}
