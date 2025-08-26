package com.example.wiseai_dev.payment.infrastructure.persistence.repository;

import com.example.wiseai_dev.payment.domain.model.Payment;
import com.example.wiseai_dev.payment.domain.model.PaymentProvider;
import com.example.wiseai_dev.payment.domain.repository.PaymentRepository;
import com.example.wiseai_dev.payment.infrastructure.persistence.entity.PaymentEntity;
import com.example.wiseai_dev.payment.infrastructure.persistence.entity.PaymentProviderEntity;
import com.example.wiseai_dev.payment.infrastructure.persistence.jpa.PaymentJpaRepository;
import com.example.wiseai_dev.reservation.domain.model.Reservation;
import com.example.wiseai_dev.reservation.domain.model.ReservationStatus;
import com.example.wiseai_dev.reservation.infrastructrue.presistence.entity.ReservationEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {

    private final PaymentJpaRepository jpaRepository;

    @Override
    public Payment save(Payment payment) {
        PaymentEntity entity = toEntity(payment);
        PaymentEntity savedEntity = jpaRepository.save(entity);
        return fromEntity(savedEntity);
    }

    @Override
    public Optional<Payment> findById(Long id) {
        return jpaRepository.findById(id).map(this::fromEntity);
    }

    @Override
    public Optional<Payment> findByReservationId(Long reservationId) {
        return jpaRepository.findByReservationId(reservationId)
                .map(this::fromEntity);
    }

    @Override
    public Optional<Payment> findByReservationIdForUpdate(Long reservationId) {
        return jpaRepository.findByReservationIdForUpdate(reservationId)
                .map(this::fromEntity);
    }

    // --- 변환 메서드 ---
    private PaymentEntity toEntity(Payment domainModel) {
        if (domainModel == null) return null;

        PaymentEntity entity = new PaymentEntity();
        entity.setId(domainModel.getId());

        // Reservation 매핑
        if (domainModel.getReservation() != null && domainModel.getReservation().getId() != null) {
            ReservationEntity reservationEntity = new ReservationEntity();
            reservationEntity.setId(domainModel.getReservation().getId());
            entity.setReservation(reservationEntity);
        }

        // PaymentProvider 매핑
        if (domainModel.getPaymentProvider() != null && domainModel.getPaymentProvider().getId() != null) {
            PaymentProviderEntity providerEntity = new PaymentProviderEntity();
            providerEntity.setId(domainModel.getPaymentProvider().getId());
            entity.setPaymentProvider(providerEntity);
        }

        entity.setStatus(domainModel.getStatus());
        entity.setAmount(domainModel.getAmount());
        entity.setTransactionId(domainModel.getTransactionId());
        entity.setVersion(domainModel.getVersion());

        return entity;
    }

    private Payment fromEntity(PaymentEntity entity) {
        if (entity == null) return null;

        // ReservationEntity → Reservation
        Reservation reservationDomain = null;
        if (entity.getReservation() != null) {
            ReservationEntity r = entity.getReservation();
            reservationDomain = Reservation.builder()
                    .id(r.getId())
                    .meetingRoomId(r.getMeetingRoomId())
                    .startTime(r.getStartTime())
                    .endTime(r.getEndTime())
                    .bookerName(r.getBookerName())
                    .status(r.getStatus() != null ? r.getStatus() : ReservationStatus.PENDING_PAYMENT)
                    .totalAmount(r.getTotalAmount())
                    .version(r.getVersion())
                    .build();
        }

        // PaymentProviderEntity → PaymentProvider
        PaymentProvider paymentProviderDomain = null;
        if (entity.getPaymentProvider() != null) {
            PaymentProviderEntity p = entity.getPaymentProvider();
            paymentProviderDomain = PaymentProvider.builder()
                    .id(p.getId())
                    .name(p.getName())
                    .apiEndpoint(p.getApiEndpoint())
                    .authInfo(p.getAuthInfo())
                    .build();
        }

        // PaymentEntity → Payment
        return Payment.builder()
                .id(entity.getId())
                .reservation(reservationDomain)
                .paymentProvider(paymentProviderDomain)
                .status(entity.getStatus())
                .amount(entity.getAmount())
                .transactionId(entity.getTransactionId())
                .version(entity.getVersion())
                .build();
    }
}
