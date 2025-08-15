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
@Slf4j
@Component
public class VirtualAccountPaymentGateway implements PaymentGateway {

    private final PaymentJpaRepository paymentJpaRepository;

    public VirtualAccountPaymentGateway(PaymentJpaRepository paymentJpaRepository) {
        this.paymentJpaRepository = paymentJpaRepository;
    }


    @Override
    public PaymentResult processPayment(Payment payment) {
        // C사 API 호출 (가정)
        String rawResponse = "{\"code\":200, \"message\":\"가상계좌 발급 성공\", \"tid\":\"VIRT_11111\"}";

        // C사 응답을 PaymentResult 공통 모델로 변환
        return PaymentResult.builder()
                .status(PaymentStatus.SUCCESS)
                .transactionId("VIRT_11111")
                .providerName("C사_가상계좌")
                .rawResponse(rawResponse)
                .build();
    }

    @Override
    public void processWebhook(Map<String, Object> webhookData) {
        // 웹훅 데이터에서 필요한 정보(예: 거래 ID, 상태) 추출
        String transactionId = (String) webhookData.get("transactionId");
        String statusString = (String) webhookData.get("status");

        if (transactionId == null || statusString == null) {
            log.error("웹훅 데이터에 transactionId 또는 status가 누락되었습니다: {}", webhookData);
            throw new IllegalArgumentException("웹훅 데이터 형식이 올바르지 않습니다.");
        }

        try {
            // 1. 거래 ID를 통해 Payment 엔티티를 조회
            PaymentEntity paymentEntity = (PaymentEntity) paymentJpaRepository.findByTransactionId(transactionId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 거래 ID의 결제 정보를 찾을 수 없습니다: " + transactionId));

            // 2. 웹훅 상태 문자열을 Enum으로 변환
            PaymentStatus newStatus = PaymentStatus.valueOf(statusString);

            // 가상 계좌 결제는 입금 완료 시에만 상태를 변경
            if (newStatus == PaymentStatus.SUCCESS) {
                // 3. Payment 엔티티의 상태 업데이트
                paymentEntity.setStatus(newStatus);

                // 4. 변경된 엔티티를 DB에 저장
                paymentJpaRepository.save(paymentEntity);

                log.info("VirtualAccountPaymentGateway 웹훅 처리 성공. 거래 ID: {}, 상태: {}", transactionId, newStatus);
            } else {
                log.info("VirtualAccountPaymentGateway 웹훅 수신. 거래 ID: {}의 상태가 CONFIRMED가 아니므로 처리하지 않습니다.", transactionId);
            }

        } catch (IllegalArgumentException e) {
            log.error("웹훅 처리 실패: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("웹훅 처리 중 예상치 못한 오류 발생: {}", e.getMessage());
            throw new RuntimeException("웹훅 처리 중 예상치 못한 오류 발생", e);
        }
    }
}
