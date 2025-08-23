package com.example.wiseai_dev.payment.infrastructure.persistence.gateway.impl;

import com.example.wiseai_dev.payment.domain.gateway.PaymentGateway;
import com.example.wiseai_dev.payment.domain.model.Payment;
import com.example.wiseai_dev.payment.domain.model.PaymentResult;
import com.example.wiseai_dev.payment.domain.model.PaymentStatus;
import com.example.wiseai_dev.payment.infrastructure.persistence.entity.PaymentEntity;
import com.example.wiseai_dev.payment.infrastructure.persistence.jpa.PaymentJpaRepository;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
@Component
public class CardPaymentGateway implements PaymentGateway {
    public CardPaymentGateway(PaymentJpaRepository paymentJpaRepository) {
        this.paymentJpaRepository = paymentJpaRepository;
    }

    @Override
    public PaymentResult  processPayment(Payment payment) {
        // A사 API 호출
        String rawResponse = "{\"status\":\"SUCCESS\", \"txnId\":\"CARD_12345\"}";

        return PaymentResult.builder()
                .status(PaymentStatus.SUCCESS)
                .transactionId("CARD_12345")
                .providerName("A사_카드결제")
                .rawResponse(rawResponse)
                .build();

            }

    private final PaymentJpaRepository paymentJpaRepository;

    @Override
    public void processWebhook(Map<String, Object> webhookData) {
        // 웹훅 데이터에서 필요한 정보(예: 거래 ID, 상태) 추출
        String transactionId = (String) webhookData.get("transactionId");
        String statusString = (String) webhookData.get("status");

        if (transactionId == null || statusString == null) {
//            log.error("Invalid webhook data received: {}", webhookData);
            throw new IllegalArgumentException("웹훅 데이터에 transactionId 또는 status가 누락되었습니다.");
        }

        try {
            // 1. 거래 ID를 통해 Payment 엔티티를 조회
            PaymentEntity paymentEntity = (PaymentEntity) paymentJpaRepository.findByTransactionId(transactionId)
                    .orElseThrow(() -> new IllegalArgumentException("Payment not found for transactionId: " + transactionId));

            // 2. 웹훅 상태 문자열을 Enum으로 변환
            PaymentStatus newStatus = PaymentStatus.valueOf(statusString);

            // 3. Payment 엔티티의 상태 업데이트
            paymentEntity.setStatus(newStatus);

            // 4. 변경된 엔티티를 DB에 저장
            paymentJpaRepository.save(paymentEntity);

            log.info("Successfully processed webhook for transactionId: {}. Status updated to: {}", transactionId, newStatus);

        } catch (IllegalArgumentException e) {
            log.error("Failed to process webhook: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("An unexpected error occurred while processing webhook: {}", e.getMessage());
            throw new RuntimeException("웹훅 처리 중 예상치 못한 오류 발생", e);
        }
    }
}
