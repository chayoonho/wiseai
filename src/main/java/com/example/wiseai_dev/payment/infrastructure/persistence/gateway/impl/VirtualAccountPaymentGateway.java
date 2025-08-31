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
public class VirtualAccountPaymentGateway implements PaymentGateway {

    private final PaymentJpaRepository paymentJpaRepository;

    public VirtualAccountPaymentGateway(PaymentJpaRepository paymentJpaRepository) {
        this.paymentJpaRepository = paymentJpaRepository;
    }

    /**
     * Mock 가상계좌 결제 처리 (C사)
     * 실제 PG라면 "가상계좌 발급 성공" 응답을 반환하고,
     * 나중에 입금이 완료되면 웹훅이 호출됨.
     */
    @Override
    public PaymentResult processPayment(Payment payment) {
        // 고유 TransactionId 생성
        String generatedId = "VIRT_" + UUID.randomUUID().toString().substring(0, 8);

        // Mock 응답 JSON
        String rawResponse = String.format(
                "{\"code\":200, \"message\":\"가상계좌 발급 성공\", \"tid\":\"%s\"}",
                generatedId
        );

        log.info("[VirtualAccountPaymentGateway] Mock 가상계좌 발급 성공. transactionId={}, amount={}", generatedId, payment.getAmount());

        return PaymentResult.builder()
                .status(PaymentStatus.PENDING) // 가상계좌는 입금 전까지 대기 상태
                .transactionId(generatedId)
                .providerName("C사_가상계좌")
                .rawResponse(rawResponse)
                .build();
    }

    /**
     * Mock 웹훅 처리
     * 실제로는 PG사에서 "입금 완료" 알림을 보냄
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

            // 3. 입금 완료 시에만 상태 업데이트
            if (newStatus == PaymentStatus.SUCCESS) {
                paymentEntity.setStatus(newStatus);
                paymentJpaRepository.save(paymentEntity);

                log.info("[VirtualAccountPaymentGateway] 입금 완료 처리 성공. transactionId={}, status={}", transactionId, newStatus);
            } else {
                log.info("[VirtualAccountPaymentGateway] 웹훅 수신. transactionId={}, status={} → 입금 대기 상태 유지", transactionId, newStatus);
            }

        } catch (IllegalArgumentException e) {
            log.error("웹훅 처리 실패: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("웹훅 처리 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("웹훅 처리 중 예상치 못한 오류 발생", e);
        }
    }
}
