package com.example.wiseai_dev.payment.infrastructure.persistence.entity;

import com.example.wiseai_dev.payment.domain.model.Payment;
import com.example.wiseai_dev.payment.domain.model.PaymentProvider;
import com.example.wiseai_dev.payment.domain.model.PaymentStatus;
import com.example.wiseai_dev.reservation.domain.model.Reservation;
import com.example.wiseai_dev.reservation.infrastructure.persistence.entity.ReservationEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
public class PaymentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private ReservationEntity reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private PaymentProviderEntity paymentProvider;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private double amount;

    private String transactionId;

    @Version
    private Long version;


    public Payment toPayment() {
        // ReservationEntity를 Reservation 도메인 모델로 변환
//        Reservation reservationDomain = this.reservation.toDomainModel();
        PaymentProvider paymentProviderDomain = this.paymentProvider.toDomainModel();

        return Payment.builder()
                .id(this.id)
//                .reservation(paymentProviderDomain)
                .paymentProvider(paymentProviderDomain)
                .status(this.status)
                .amount(this.amount)
                .transactionId(this.transactionId)
                .version(this.version)
                .build();
    }


}