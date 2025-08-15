package com.example.wiseai_dev.payment.application.api.controller;

import com.example.wiseai_dev.payment.application.api.dto.PaymentRequest;
import com.example.wiseai_dev.payment.application.api.dto.PaymentResponse;
import com.example.wiseai_dev.payment.application.service.PaymentService;
import com.example.wiseai_dev.payment.domain.model.PaymentResult;
import com.example.wiseai_dev.payment.domain.model.PaymentStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reservations")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/{id}/payment")
    public ResponseEntity<PaymentResponse> processPayment(@PathVariable("id") Long reservationId,
                                                          @RequestBody PaymentRequest request) {
        PaymentResult paymentResult = paymentService.processReservationPayment(
                reservationId,
                request.getPaymentProviderName()
        );

        PaymentResponse response = new PaymentResponse(
                paymentResult.getPaymentId(),
                paymentResult.getReservationId(),
                paymentResult.getStatus(),
                paymentResult.getAmount(),
                paymentResult.getTransactionId()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{reservationId}/status")
    public ResponseEntity<PaymentStatus> getPaymentStatus(
            @PathVariable Long reservationId) {

        try {
            PaymentStatus status = paymentService.getPaymentStatus(reservationId);
            return ResponseEntity.ok(status);
        } catch (IllegalArgumentException e) {
            // 결제 정보가 없을 경우 404 Not Found 반환
            return ResponseEntity.notFound().build();

        }
    }
}