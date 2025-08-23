package com.example.wiseai_dev.payment.application.api.controller;

import com.example.wiseai_dev.payment.application.api.dto.PaymentRequest;
import com.example.wiseai_dev.payment.application.api.dto.PaymentResponse;
import com.example.wiseai_dev.payment.application.service.PaymentService;
import com.example.wiseai_dev.payment.domain.model.PaymentStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reservations")
@Tag(name = "Payment API", description = "회의실 예약 결제 처리 API")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * 예약 결제 처리
     */
    @Operation(summary = "예약 결제 요청", description = "특정 예약에 대해 결제를 처리합니다.")
    @PostMapping("/{id}/payment")
    public ResponseEntity<PaymentResponse> processPayment(
            @PathVariable("id") Long reservationId,
            @RequestBody PaymentRequest request) {

        PaymentResponse response = paymentService.processReservationPayment(
                reservationId,
                request.getPaymentProviderName()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 결제 상태 조회
     */
    @Operation(summary = "결제 상태 조회", description = "예약 ID를 기반으로 결제 상태를 조회합니다.")
    @GetMapping("/{reservationId}/status")
    public ResponseEntity<PaymentStatus> getPaymentStatus(
            @PathVariable Long reservationId) {

        try {
            PaymentStatus status = paymentService.getPaymentStatus(reservationId);
            return ResponseEntity.ok(status);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
