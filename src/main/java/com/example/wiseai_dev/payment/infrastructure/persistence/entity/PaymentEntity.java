package com.example.wiseai_dev.payment.infrastructure.persistence.entity;

import com.example.wiseai_dev.payment.domain.model.Payment;
import com.example.wiseai_dev.payment.domain.model.PaymentProvider;
import com.example.wiseai_dev.payment.domain.model.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "payments", uniqueConstraints = @UniqueConstraint(columnNames = "transactionId"))
@Getter
@Setter
@NoArgsConstructor
public class PaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 웹훅 전용으로 Reservation ID만 저장
    @Column(name = "reservation_id", nullable = false)
    private Long reservationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private PaymentProviderEntity paymentProvider;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private double amount;

    @Column(nullable = false, unique = true)
    private String transactionId;

    @Column(columnDefinition = "TEXT")
    private String rawResponse; // 웹훅 원본 데이터 저장용

    @Version
    private Long version;

    // 도메인 모델로 변환
    public Payment toPayment() {
        PaymentProvider paymentProviderDomain = this.paymentProvider.toDomainModel();

        return Payment.builder()
                .id(this.id)
                .reservationId(this.reservationId) // Reservation ID만 저장
                .paymentProvider(paymentProviderDomain)
                .status(this.status)
                .amount(this.amount)
                .transactionId(this.transactionId)
                .version(this.version)
                .build();
    }
}
