package com.example.wiseai_dev.payment.application.api.dto;

import com.example.wiseai_dev.payment.domain.model.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Long paymentId;
    private Long reservationId;
    private PaymentStatus status;
    private double amount;
    private String transactionId;
}