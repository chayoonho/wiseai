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
public class SimplePaymentGateway implements PaymentGateway {

    private final PaymentJpaRepository paymentJpaRepository;

    public SimplePaymentGateway(PaymentJpaRepository paymentJpaRepository) {
        this.paymentJpaRepository = paymentJpaRepository;
    }

    /**
     * Mock 간편결제 처리 (B사)
     */
    @Override
    public PaymentResult processPayment(Payment payment) {
        // 고유 TransactionId 생성
        String generatedId = "SIMPLE_" + UUID.randomUUID().toString().substring(0, 8);

        // Mock 응답 XML (B사 응답 형식 가정)
        String rawResponse = String.format("<result><state>OK</state><tid>%s</tid></result>", generatedId);

        log.info("[SimplePaymentGateway] Mock 결제 성공. transactionId={}, amount={}", generatedId, payment.getAmount());

        return PaymentResult.builder()
                .status(PaymentStatus.SUCCESS)
                .transactionId(generatedId)    
                .providerName("B사_간편결제")
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
            log.error("웹훅 데이터에 transactionId 또는 status가 누락됨: {}", webhookData);
            throw new IllegalArgumentException("웹훅 데이터 형식이 올바르지 않습니다.");
        }

        try {
            // 1. 거래 ID로 Payment 엔티티 조회
            PaymentEntity paymentEntity = paymentJpaRepository.findByTransactionId(transactionId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 거래 ID의 결제 정보를 찾을 수 없습니다: " + transactionId));

            // 2. 상태 문자열 → Enum 변환
            PaymentStatus newStatus = PaymentStatus.valueOf(statusString);

            // 3. 상태 업데이트
            paymentEntity.setStatus(newStatus);

            // 4. 저장
            paymentJpaRepository.save(paymentEntity);

            log.info("[SimplePaymentGateway] 웹훅 처리 성공. transactionId={}, status={}", transactionId, newStatus);

        } catch (IllegalArgumentException e) {
            log.error("웹훅 처리 실패: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("웹훅 처리 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("웹훅 처리 중 예상치 못한 오류 발생", e);
        }
    }
}
