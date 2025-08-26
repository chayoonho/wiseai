package com.example.wiseai_dev.payment.infrastructure.persistence.jpa;

import com.example.wiseai_dev.payment.infrastructure.persistence.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, Long> {

    // 예약 ID 기반 결제 내역 조회 (비관적 락 적용)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PaymentEntity p where p.reservation.id = :reservationId")
    Optional<PaymentEntity> findByReservationIdForUpdate(@Param("reservationId") Long reservationId);

    // 트랜잭션 ID 기반 조회
    Optional<PaymentEntity> findByTransactionId(String transactionId);

    Optional<PaymentEntity> findByReservation_Id(Long reservationId);

    // 예약 ID 기반 삭제
    void deleteByReservationId(Long reservationId);

    Optional<PaymentEntity> findByReservationId(Long reservationId);

}
