package com.example.wiseai_dev.payment.infrastructure.persistence.entity;

import com.example.wiseai_dev.payment.domain.model.Payment;
import com.example.wiseai_dev.payment.domain.model.PaymentProvider;
import com.example.wiseai_dev.payment.domain.model.PaymentStatus;
import com.example.wiseai_dev.reservation.infrastructrue.presistence.entity.ReservationEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "payments",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_transaction_id", columnNames = "transactionId"),
                @UniqueConstraint(name = "uk_reservation_id", columnNames = "reservation_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class PaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reservation_id", nullable = false)
    private ReservationEntity reservation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "provider_id", nullable = false)
    private PaymentProviderEntity paymentProvider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(nullable = false)
    private double amount;

    @Column(nullable = false, unique = true)
    private String transactionId;

    @Column(columnDefinition = "TEXT")
    private String rawResponse;

    @Version
    private Long version;

    public Payment toPayment() {
        PaymentProvider provider = this.paymentProvider.toDomainModel();

        return Payment.builder()
                .id(this.id)
                .paymentProvider(provider)
                .status(this.status)
                .amount(this.amount)
                .transactionId(this.transactionId)
                .version(this.version)
                .build();
    }
}