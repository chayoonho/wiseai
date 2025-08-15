package com.example.wiseai_dev.payment.application.api.controller;

import com.example.wiseai_dev.payment.application.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/webhooks/payments")
public class PaymentWebhookController {

    private final PaymentService paymentService;

    public PaymentWebhookController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/{provider}")
    public ResponseEntity<Void> handleWebhook(
            @PathVariable String provider,
            @RequestBody Map<String, Object> webhookData) {
        try {
            paymentService.handleWebhook(provider, webhookData);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {

            // 지원하지 않는 결제사, 또는 잘못된 데이터일 경우 400 Bad Request 반환
            return ResponseEntity.badRequest().build();
        }
    }
}
