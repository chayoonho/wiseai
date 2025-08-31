package com.example.wiseai_dev.payment.application.api.controller;

import com.example.wiseai_dev.global.ApiResponse;
import com.example.wiseai_dev.payment.application.api.dto.PaymentRequest;
import com.example.wiseai_dev.payment.application.api.dto.PaymentResponse;
import com.example.wiseai_dev.payment.application.service.PaymentService;
import com.example.wiseai_dev.payment.domain.model.PaymentStatus;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "예약 결제 처리")
    @PostMapping("/{id}/payment")
    public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(
            @PathVariable("id") Long reservationId,
            @RequestBody PaymentRequest request) {

        PaymentResponse response = paymentService.processReservationPayment(
                reservationId,
                request.getPaymentProviderName()
        );

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "결제 상태 조회")
    @GetMapping("/{reservationId}/status")
    public ResponseEntity<ApiResponse<PaymentStatus>> getPaymentStatus(
            @PathVariable Long reservationId) {

        PaymentStatus status = paymentService.getPaymentStatus(reservationId);
        return ResponseEntity.ok(ApiResponse.ok(status));
    }
}
