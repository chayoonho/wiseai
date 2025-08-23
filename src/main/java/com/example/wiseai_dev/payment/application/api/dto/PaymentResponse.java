package com.example.wiseai_dev.payment.application.api.dto;

import com.example.wiseai_dev.payment.domain.model.Payment;
import com.example.wiseai_dev.payment.domain.model.PaymentStatus;
import com.example.wiseai_dev.reservation.domain.model.Reservation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "결제 응답 DTO")
public class PaymentResponse {

    private Long paymentId;
    private Long reservationId;
    private double amount;
    private String providerName;
    private PaymentStatus status;
    private String transactionId;
    private String reservationStatus;

    public static PaymentResponse from(Payment payment, Reservation reservation) {
        return PaymentResponse.builder()
                .paymentId(payment.getId())
                .reservationId(reservation.getId())
                .amount(payment.getAmount())
                .providerName(payment.getPaymentProvider().getName())
                .status(payment.getStatus())
                .transactionId(payment.getTransactionId())
                .reservationStatus(reservation.getStatus().name())
                .build();
    }
}
