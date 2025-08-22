package com.example.wiseai_dev.payment.application.service;

import com.example.wiseai_dev.payment.domain.gateway.PaymentGateway;
import com.example.wiseai_dev.payment.domain.model.Payment;
import com.example.wiseai_dev.payment.domain.model.PaymentProvider;
import com.example.wiseai_dev.payment.domain.model.PaymentResult;
import com.example.wiseai_dev.payment.domain.model.PaymentStatus;
import com.example.wiseai_dev.payment.domain.repository.PaymentProviderRepository;
import com.example.wiseai_dev.payment.domain.repository.PaymentRepository;
import com.example.wiseai_dev.payment.infrastructure.persistence.entity.PaymentEntity;
import com.example.wiseai_dev.payment.infrastructure.persistence.jpa.PaymentJpaRepository;
import com.example.wiseai_dev.reservation.domain.model.Reservation;
import com.example.wiseai_dev.reservation.domain.model.ReservationStatus;
import com.example.wiseai_dev.reservation.domain.repository.ReservationRepository;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class PaymentService {

    private final ReservationRepository reservationRepository;
    private final PaymentProviderRepository paymentProviderRepository;
    private final PaymentRepository paymentRepository;
    private final Map<String, PaymentGateway> paymentGateways;
    private final PaymentJpaRepository paymentJpaRepository;

    public PaymentService(ReservationRepository reservationRepository,
                          PaymentProviderRepository paymentProviderRepository,
                          PaymentRepository paymentRepository,
                          List<PaymentGateway> gateways, PaymentJpaRepository paymentJpaRepository) {
        this.reservationRepository = reservationRepository;
        this.paymentProviderRepository = paymentProviderRepository;
        this.paymentRepository = paymentRepository;
        this.paymentGateways = gateways.stream()
                .collect(Collectors.toMap(
                        gateway -> gateway.getClass().getSimpleName().replace("PaymentGateway", ""),
                        Function.identity()
                ));
        this.paymentJpaRepository = paymentJpaRepository;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public PaymentResult processReservationPayment(Long reservationId, String paymentProviderName) {
        // 1. 비관적 락으로 예약 조회 (데드락 방지)
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다."));

        if (reservation.getStatus() != ReservationStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("결제가 가능한 예약 상태가 아닙니다.");
        }

        //  결제 관련 로직은 제거하고, 예약 상태만 변경하여 버전 충돌을 유도 테스트용 ***
//        reservation.confirmPayment();
//        reservationRepository.save(reservation);
//
//        return null;

        // 2. 결제 정보 중복 체크 (선택사항, 필요에 따라 유지)
        if (paymentRepository.findByReservationId(reservationId).isPresent()) {
            throw new IllegalStateException("이미 결제 정보가 존재하는 예약입니다.");
        }

        // 3. 결제 게이트웨이 호출 (Payment 객체 생성)
        PaymentProvider paymentProvider = paymentProviderRepository.findByName(paymentProviderName)
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 결제사입니다.==> " + paymentProviderName));
        PaymentGateway gateway = paymentGateways.get(paymentProviderName);
        Payment newPayment = new Payment(
                null, reservation, paymentProvider, PaymentStatus.PENDING,
                reservation.getTotalAmount(), null, 0L
        );
        PaymentResult paymentResult = gateway.processPayment(newPayment);

        // 4. 결제 상태 업데이트 및 저장 (새로운 트랜잭션에서 실행)
        try {
            Payment savedPayment = savePayment(newPayment, paymentResult);

            // 5. 예약 상태 업데이트 (현재 트랜잭션에서 진행)
            if (savedPayment.getStatus() == PaymentStatus.SUCCESS) {
                reservation.setStatus(ReservationStatus.CONFIRMED);
            } else {
                reservation.setStatus(ReservationStatus.CANCELLED);
            }
            reservationRepository.save(reservation);

            // 6. 결과 반환
            return PaymentResult.builder()
                    .paymentId(savedPayment.getId())
                    .reservationId(reservation.getId())
                    .amount(savedPayment.getAmount())
                    .status(savedPayment.getStatus())
                    .transactionId(savedPayment.getTransactionId())
                    .providerName(paymentProviderName)
                    .rawResponse(paymentResult.getRawResponse())
                    .build();
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new IllegalStateException("동시성 충돌 발생. 결제 정보를 다시 확인해주세요.");
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Payment savePayment(Payment newPayment, PaymentResult paymentResult) {
        newPayment.setTransactionId(paymentResult.getTransactionId());
        newPayment.setStatus(paymentResult.getStatus());
        return paymentRepository.save(newPayment);
    }

    public PaymentStatus getPaymentStatus(Long reservationId) {
        PaymentEntity paymentEntity = paymentJpaRepository.findByReservationId(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("해당 예약에 대한 결제 정보가 없습니다."));

        Payment payment = paymentEntity.toPayment();

        return payment.getStatus();
    }

    public void handleWebhook(String provider, Map<String, Object> webhookData) {
        PaymentGateway paymentGateway = paymentGateways.get(provider);
        if (paymentGateway == null) {
            throw new IllegalArgumentException("Unsupported payment provider: " + provider);
        }

        // 결제 게이트웨이를 통해 웹훅 데이터를 처리하고 Payment 객체를 업데이트
        paymentGateway.processWebhook(webhookData);

    }

}