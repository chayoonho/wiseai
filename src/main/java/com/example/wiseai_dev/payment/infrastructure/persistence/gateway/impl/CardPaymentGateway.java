package com.example.wiseai_dev.payment.infrastructure.persistence.gateway.impl;

import com.example.wiseai_dev.payment.domain.gateway.PaymentGateway;
import com.example.wiseai_dev.payment.domain.model.Payment;
import com.example.wiseai_dev.payment.domain.model.PaymentResult;
import com.example.wiseai_dev.payment.domain.model.PaymentStatus;
import com.example.wiseai_dev.payment.infrastructure.persistence.entity.PaymentEntity;
import com.example.wiseai_dev.payment.infrastructure.persistence.jpa.PaymentJpaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class CardPaymentGateway implements PaymentGateway {

    private final PaymentJpaRepository paymentJpaRepository;

    public CardPaymentGateway(PaymentJpaRepository paymentJpaRepository) {
        this.paymentJpaRepository = paymentJpaRepository;
    }

    /**
     * Mock 결제 처리 (A사 카드결제)
     */
    @Override
    public PaymentResult processPayment(Payment payment) {
        // 실제 PG사 API 대신 가짜 응답 생성
        String generatedId = "CARD_" + UUID.randomUUID().toString().substring(0, 8); //
        String rawResponse = String.format("{\"status\":\"SUCCESS\", \"txnId\":\"%s\"}", generatedId);

        log.info("[CardPaymentGateway] Mock 결제 성공. transactionId={}, amount={}", generatedId, payment.getAmount());

        return PaymentResult.builder()
                .status(PaymentStatus.SUCCESS)
                .transactionId(generatedId)    //
                .providerName("A사_카드결제")
                .rawResponse(rawResponse)
                .build();
    }

    /**
     * Mock 웹훅 처리
     */
    @Override
    public void processWebhook(Map<String, Object> webhookData) {
        String transactionId = (String) webhookData.get("transactionId");
        String statusString = (String) webhookData.get("status");

        if (transactionId == null || statusString == null) {
            throw new IllegalArgumentException("웹훅 데이터에 transactionId 또는 status가 누락되었습니다.");
        }

        try {
            // 1. 거래 ID로 Payment 엔티티 조회
            PaymentEntity paymentEntity = paymentJpaRepository.findByTransactionId(transactionId)
                    .orElseThrow(() -> new IllegalArgumentException("Payment not found for transactionId: " + transactionId));

            // 2. 상태 업데이트
            PaymentStatus newStatus = PaymentStatus.valueOf(statusString);
            paymentEntity.setStatus(newStatus);

            // 3. 저장
            paymentJpaRepository.save(paymentEntity);

            log.info("[CardPaymentGateway] 웹훅 처리 완료. transactionId={}, status={}", transactionId, newStatus);

        } catch (IllegalArgumentException e) {
            log.error("Webhook 처리 실패: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("웹훅 처리 중 예기치 못한 오류: {}", e.getMessage(), e);
            throw new RuntimeException("웹훅 처리 실패", e);
        }
    }
}
