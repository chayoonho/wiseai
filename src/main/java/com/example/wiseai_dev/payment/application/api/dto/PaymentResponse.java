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

    @Schema(description = "결제 ID", example = "2001")
    private Long paymentId;

    @Schema(description = "예약 ID", example = "1001")
    private Long reservationId;

    @Schema(description = "결제 금액", example = "30000")
    private double amount;

    @Schema(description = "결제사 이름", example = "Card")
    private String providerName;

    @Schema(description = "결제 상태", example = "SUCCESS")
    private PaymentStatus status;

    @Schema(description = "거래 ID", example = "TXN-20250825-0001")
    private String transactionId;

    @Schema(description = "예약 상태", example = "CONFIRMED")
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
