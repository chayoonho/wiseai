package com.example.wiseai_dev.payment.application.api.controller;

import com.example.wiseai_dev.global.ApiResponse;
import com.example.wiseai_dev.payment.application.api.dto.ProviderPayload;
import com.example.wiseai_dev.payment.application.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhooks/payments")
@RequiredArgsConstructor
public class PaymentWebhookController {

    private final PaymentService paymentService;

    @Operation(
            summary = "PG사 Webhook 수신",
            description = "PG사(Card, Simple, VirtualAccount 등)에서 결제 결과를 서버로 전송하는 Webhook 엔드포인트입니다.",
            requestBody = @RequestBody(
                    required = true,
                    description = "PG사 Webhook에서 전달하는 결제 결과 데이터",
                    content = @Content(
                            examples = {
                                    @ExampleObject(
                                            name = "Card 예시",
                                            value = "{ \"transactionId\": \"CARD_12345\", \"status\": \"SUCCESS\", \"amount\": 30000 }"
                                    ),
                                    @ExampleObject(
                                            name = "Simple 예시",
                                            value = "{ \"transactionId\": \"SIMPLE_67890\", \"status\": \"FAILED\", \"amount\": 15000 }"
                                    ),
                                    @ExampleObject(
                                            name = "VirtualAccount 예시",
                                            value = "{ \"transactionId\": \"VIRT_11111\", \"status\": \"CANCELLED\", \"amount\": 50000 }"
                                    )
                            }
                    )
            )
    )
    @PostMapping("/{provider}")
    public ResponseEntity<ApiResponse<Void>> handleWebhook(
            @PathVariable("provider") String provider,
            @org.springframework.web.bind.annotation.RequestBody ProviderPayload payload) {

        paymentService.handleWebhook(provider, payload);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
