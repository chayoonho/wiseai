package com.example.wiseai_dev.payment.domain.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentResult {
    private Long paymentId;
    private Long reservationId;
    private double amount;
    private PaymentStatus status;
    private String transactionId;
    private String providerName;
    private String rawResponse; // 각 결제사의 원본 응답
}