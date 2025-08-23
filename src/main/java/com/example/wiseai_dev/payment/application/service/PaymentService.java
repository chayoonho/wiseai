package com.example.wiseai_dev.payment.application.service;

import com.example.wiseai_dev.payment.application.api.dto.PaymentResponse;
import com.example.wiseai_dev.payment.domain.gateway.PaymentGateway;
import com.example.wiseai_dev.payment.domain.model.Payment;
import com.example.wiseai_dev.payment.domain.model.PaymentProvider;
import com.example.wiseai_dev.payment.domain.model.PaymentResult;
import com.example.wiseai_dev.payment.domain.model.PaymentStatus;
import com.example.wiseai_dev.payment.domain.repository.PaymentProviderRepository;
import com.example.wiseai_dev.payment.domain.repository.PaymentRepository;
import com.example.wiseai_dev.reservation.domain.model.Reservation;
import com.example.wiseai_dev.reservation.domain.model.ReservationStatus;
import com.example.wiseai_dev.reservation.domain.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final ReservationRepository reservationRepository;
    private final PaymentProviderRepository paymentProviderRepository;
    private final PaymentRepository paymentRepository;
    private final Map<String, PaymentGateway> paymentGateways;

    public PaymentService(ReservationRepository reservationRepository,
                          PaymentProviderRepository paymentProviderRepository,
                          PaymentRepository paymentRepository,
                          List<PaymentGateway> gateways) {
        this.reservationRepository = reservationRepository;
        this.paymentProviderRepository = paymentProviderRepository;
        this.paymentRepository = paymentRepository;
        this.paymentGateways = gateways.stream()
                .collect(Collectors.toMap(
                        gateway -> gateway.getClass().getSimpleName().replace("PaymentGateway", ""),
                        Function.identity()
                ));
    }

    /**
     * 예약 결제 처리 (비관적 락 적용)
     */
    @Transactional
    public PaymentResponse processReservationPayment(Long reservationId, String paymentProviderName) {
        // 1. 예약 조회 (락 필요 없음, 단순 조회)
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다."));

        if (reservation.getStatus() != ReservationStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("이미 결제 처리된 예약입니다.");
        }

        // 2. 중복 결제 방지 (비관적 락 적용)
        if (paymentRepository.findByReservationIdForUpdate(reservationId).isPresent()) {
            throw new IllegalStateException("이미 결제 정보가 존재하는 예약입니다.");
        }

        // 3. 결제사 확인
        PaymentProvider paymentProvider = paymentProviderRepository.findByName(paymentProviderName)
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 결제사입니다: " + paymentProviderName));

        PaymentGateway gateway = paymentGateways.get(paymentProviderName);
        if (gateway == null) {
            throw new IllegalArgumentException("등록되지 않은 결제 게이트웨이: " + paymentProviderName);
        }

        // 4. 결제 요청 생성
        Payment newPayment = new Payment(
                null, reservation, paymentProvider, PaymentStatus.PENDING,
                reservation.getTotalAmount(), null, 0L
        );

        try {
            // 5. PG사 호출
            PaymentResult paymentResult = gateway.processPayment(newPayment);

            // 6. Payment 저장
            newPayment.setTransactionId(paymentResult.getTransactionId());
            newPayment.setStatus(paymentResult.getStatus());
            Payment savedPayment = paymentRepository.save(newPayment);

            // 7. Reservation 상태 업데이트
            if (paymentResult.getStatus() == PaymentStatus.SUCCESS) {
                reservation.setStatus(ReservationStatus.CONFIRMED);
            } else {
                reservation.setStatus(ReservationStatus.CANCELLED);
            }
            reservationRepository.save(reservation);

            // 8. DTO 응답 반환
            return PaymentResponse.from(savedPayment, reservation);

        } catch (ObjectOptimisticLockingFailureException e) {
            throw new IllegalStateException("동시성 충돌 발생. 결제를 재시도해주세요.");
        }
    }
}
