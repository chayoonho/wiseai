package com.example.wiseai_dev.payment.infrastructure.persistence.repository;

import com.example.wiseai_dev.payment.domain.model.Payment;
import com.example.wiseai_dev.payment.domain.model.PaymentProvider;
import com.example.wiseai_dev.payment.domain.repository.PaymentRepository;
import com.example.wiseai_dev.payment.infrastructure.persistence.entity.PaymentEntity;
import com.example.wiseai_dev.payment.infrastructure.persistence.entity.PaymentProviderEntity;
import com.example.wiseai_dev.payment.infrastructure.persistence.jpa.PaymentJpaRepository;
import com.example.wiseai_dev.reservation.domain.model.Reservation;
import com.example.wiseai_dev.reservation.infrastructure.persistence.entity.ReservationEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class PaymentRepositoryImpl implements PaymentRepository {

    private final PaymentJpaRepository jpaRepository;

    public PaymentRepositoryImpl(PaymentJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Payment save(Payment payment) {
        PaymentEntity entity = toEntity(payment);
        PaymentEntity savedEntity = jpaRepository.save(entity);
        return fromEntity(savedEntity);
    }

    @Override
    public Optional<Payment> findById(Long id) {
        Optional<PaymentEntity> entity = jpaRepository.findById(id);
        return entity.map(this::fromEntity);
    }

    @Override
    public Optional<Object> findByReservationId(Long reservationId) {
        Optional<PaymentEntity> entity = jpaRepository.findByReservationId(reservationId);
        // Payment 도메인 모델로 변환하여 반환
        return entity.map(this::fromEntity);
    }

    // --- 변환 헬퍼 메서드 ---
    private PaymentEntity toEntity(Payment domainModel) {
        if (domainModel == null) {
            return null;
        }
        PaymentEntity entity = new PaymentEntity();
        entity.setId(domainModel.getId());

        // Null 체크 후 ReservationEntity 설정
        if (domainModel.getReservation() != null && domainModel.getReservation().getId() != null) {
            ReservationEntity reservationEntity = new ReservationEntity();
            reservationEntity.setId(domainModel.getReservation().getId());
            entity.setReservation(reservationEntity);
        }

        // PaymentProvider 객체 변환 로직 추가
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
        if (entity == null) {
            return null;
        }

        // ReservationEntity를 Reservation 도메인 모델로 변환
        Reservation reservationDomain = null;
        if (entity.getReservation() != null) {
            ReservationEntity reservationEntity = entity.getReservation();
            reservationDomain = new Reservation(
                                reservationEntity.getId(),
                                reservationEntity.getReservationNo(),
                                reservationEntity.getMeetingRoomId(),
                                reservationEntity.getStartTime(),
                                reservationEntity.getEndTime(),
                                reservationEntity.getBookerName(),
                                reservationEntity.getStatus(),
                                reservationEntity.getTotalAmount(),
                                reservationEntity.getVersion()
                        );
        }

        // PaymentProviderEntity를 PaymentProvider 도메인 모델로 변환
        PaymentProvider paymentProviderDomain = null;
        if (entity.getPaymentProvider() != null) {
            PaymentProviderEntity providerEntity = entity.getPaymentProvider();
            paymentProviderDomain = new PaymentProvider(
                    providerEntity.getId(),
                    providerEntity.getName(),
                    providerEntity.getApiEndpoint(),
                    providerEntity.getAuthInfo()
            );
        }

        return new Payment(
                entity.getId(),
                reservationDomain,
                paymentProviderDomain,
                entity.getStatus(),
                entity.getAmount(),
                entity.getTransactionId(),
                entity.getVersion()
        );
    }
}